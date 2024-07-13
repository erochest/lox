package com.ericrochester.klox

import com.ericrochester.klox.TokenType.*
import com.ericrochester.klox.app.error as loxError
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

// The parser so far.
// program        → declaration* EOF ;
//
// declaration    → classDecl
//                | funDecl
//                | varDecl
//                | statement ;
//
// statement      → exprStmt
//                | forStmt
//                | ifStmt
//                | printStmt
//                | returnStmt
//                | whileStmt
//                | block ;
//
// block          → "{" declaration* "}" ;
//
// classDecl      → "class" IDENTIFIER "{" function* "}" ;
// funDecl        → "fun" function ;
// function       → IDENTIFIER "(" parameters? ")" block ;
// parameters     → IDENTIFIER ( "," IDENTIFIER )* ;
// varDecl        → "var" IDENTIFIER ( "=" expression )? ";" ;
// exprStmt       → expression ";" ;
// forStmt        → "for" "(" ( varDecl | exprStmt | ";" )
//                  expression? ";"
//                  expression? ")" statement ;
// ifStmt         → "if" "(" expression ")" statement
//                ( "else" statement )? ;
// printStmt      → "print" expression ";" ;
// returnStmt     → "return" expression ";" ;
// whileStmt      → "while" "(" expression ")" statement ;
// expression     → assignment ;
// assignment     → IDENTIFIER "=" assignment
//                | logic_or ;
// logic_or       → lagic_and ( "or" logic_and )* ;
// logic_and      → equality ( "or" equality )* ;
// equality       → comparison ( ( "!=" | "==" ) comparison )* ;
// comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
// term           → factor ( ( "-" | "+" ) factor )* ;
// factor         → unary ( ( "/" | "*" ) unary )* ;
// unary          → ( "!" | "-" ) unary
//                | call ;
// call           → primary ( "(" arguments? ")" )* ;
// arguments      → expression ( "," expression )* ;
// primary        → NUMBER | STRING | "true" | "false" | "nil"
//                | "(" expression ")"
//                | IDENTIFIER ;

class Parser(private val tokens: List<Token>) {
  private class ParseError : RuntimeException()

  private var current = 0

  // Interface

  fun parse(): List<Stmt?> {
    logger.debug { "Starting parse: " + peek().toString() }
    val statements: MutableList<Stmt?> = mutableListOf()
    while (!isAtEnd()) {
      statements.add(declaration())
    }

    return statements
  }

  // Productions
  private fun declaration(): Stmt? {
    try {
      if (match(CLASS)) return classDeclaration()
      if (match(FUN)) return function("function")
      if (match(VAR)) return varDeclaration()
      return statement()
    } catch (error: ParseError) {
      synchronize()
      return null
    }
  }

  private fun classDeclaration(): Stmt {
    val name = consume(IDENTIFIER, "Expect class name.")
    consume(LEFT_BRACE, "Expect '{' before class body.")
    val methods = mutableListOf<Function>()

    while (!check(RIGHT_BRACE) && !isAtEnd()) {
      methods.add(function("method"))
    }

    consume(RIGHT_BRACE, "Expect '}' after class body.")
    return ClassStmt(name, methods)
  }

  private fun statement(): Stmt {
    if (match(PRINT)) return printStatement()
    if (match(RETURN)) return returnStatement()
    if (match(WHILE)) return whileStatement()
    if (match(LEFT_BRACE)) return Block(block())
    if (match(IF)) return ifStatement()
    if (match(FOR)) return forStatement()
    return expressionStatement()
  }

  private fun printStatement(): Stmt {
    val value = expression()
    consume(SEMICOLON, "Expect ';' after value.")
    return Print(value)
  }

  private fun returnStatement(): Stmt {
    val keyword = previous()
    var value: Expr? = null

    if (!check(SEMICOLON)) {
      value = expression()
    }

    consume(SEMICOLON, "Expect ';' after a return value.")
    return ReturnStmt(keyword, value)
  }

  private fun whileStatement(): Stmt {
    consume(LEFT_PAREN, "Expect '(' after 'while'.")
    val condition = expression()
    consume(RIGHT_PAREN, "Expect ')' after condition.")
    val body = statement()

    return While(condition, body)
  }

  private fun expressionStatement(): Stmt {
    val value = expression()
    consume(SEMICOLON, "Expect ';' after expression.")
    return Expression(value)
  }

  private fun function(kind: String): Function {
    val name = consume(IDENTIFIER, "Expect $kind name.")
    val parameters: MutableList<Token> = mutableListOf()

    consume(LEFT_PAREN, "Expect '(' after $kind name.")
    if (!check(RIGHT_PAREN)) {
      do {
        if (parameters.size >= 255) {
          error(peek(), "Cannot have more than 255 parameters.")
        }

        parameters.add(consume(IDENTIFIER, "Expect parameter name."))
      } while (match(COMMA))
    }
    consume(RIGHT_PAREN, "Expect ')' after parameters")

    consume(LEFT_BRACE, "Expect '{' before $kind body.")
    val body = block().filterNotNull()

    return Function(name, parameters, body)
  }

  private fun block(): List<Stmt?> {
    val statements: MutableList<Stmt?> = mutableListOf()

    while (!check(RIGHT_BRACE) && !isAtEnd()) {
      statements.add(declaration())
    }

    consume(RIGHT_BRACE, "Expect '}' after block.")
    return statements
  }

  private fun ifStatement(): Stmt {
    consume(LEFT_PAREN, "Expect '(' after 'if'.")
    val condition = expression()
    consume(RIGHT_PAREN, "Expect ')' after if condition.")

    val thenBranch = statement()
    var elseBranch: Stmt? = null
    if (match(ELSE)) {
      elseBranch = statement()
    }
    return If(condition, thenBranch, elseBranch)
  }

  private fun forStatement(): Stmt {
    consume(LEFT_PAREN, "Expect '(' after 'for'.")

    val initializer: Stmt? =
        if (match(SEMICOLON)) {
          null
        } else if (match(VAR)) {
          varDeclaration()
        } else {
          expressionStatement()
        }

    val condition = if (!check(SEMICOLON)) expression() else null
    consume(SEMICOLON, "Expect ';' after loop condition.")

    val increment = if (!check(RIGHT_PAREN)) expression() else null
    consume(RIGHT_PAREN, "Expect ')' after for clauses.")

    var body = statement()

    increment?.let { body = Block(listOf(body, Expression(it))) }
    body = While(condition ?: Literal(true), body)
    initializer?.let { body = Block(listOf(it, body)) }

    return body
  }

  private fun varDeclaration(): Stmt {
    val name = consume(IDENTIFIER, "Expect variable name.")

    var initializer: Expr? = null
    if (match(EQUAL)) {
      initializer = expression()
    }

    consume(SEMICOLON, "Expect ';' after variable declaration.")
    return Var(name, initializer)
  }

  private fun expression(): Expr {
    logger.debug { "Parsing expression: " + peek().toString() }

    if (match(
            COMMA,
            QUESTION,
            BANG_EQUAL,
            EQUAL_EQUAL,
            GREATER,
            GREATER_EQUAL,
            LESS,
            LESS_EQUAL,
            PLUS,
            SLASH,
            STAR
        )
    ) {
      throw error(previous(), "Unexpected binary operator at start of expression.")
    }
    return assignment()
  }

  private fun assignment(): Expr {
    val expr = or()

    if (match(EQUAL)) {
      val equals = previous()
      val value = assignment()

      if (expr is Variable) {
        val name = expr.name
        return Assign(name, value)
      }
      throw error(equals, "Invalid assignment target.")
    }

    return expr
  }

  private fun or(): Expr {
    var expr = and()

    while (match(OR)) {
      val operator = previous()
      val right = and()
      expr = Logical(expr, operator, right)
    }

    return expr
  }

  private fun and(): Expr {
    var expr = equality()

    while (match(AND)) {
      val operator = previous()
      val right = equality()
      expr = Logical(expr, operator, right)
    }

    return expr
  }

  private fun equality(): Expr {
    logger.debug { "Parsing equality: " + peek().toString() }
    return leftAssociativeBinary(arrayOf(BANG_EQUAL, EQUAL_EQUAL)) { comparison() }
  }

  private fun comparison(): Expr {
    logger.debug { "Parsing comparison: " + peek().toString() }
    return leftAssociativeBinary(arrayOf(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) { term() }
  }

  private fun term(): Expr {
    logger.debug { "Parsing term: " + peek().toString() }
    return leftAssociativeBinary(arrayOf(MINUS, PLUS)) { factor() }
  }

  private fun factor(): Expr {
    logger.debug { "Parsing factor: " + peek().toString() }
    return leftAssociativeBinary(arrayOf(SLASH, STAR)) { unary() }
  }

  private fun unary(): Expr {
    logger.debug { "Parsing unary: " + peek().toString() }
    if (match(BANG, MINUS)) {
      val op = previous()
      val right = unary()
      return Unary(op, right)
    }
    return call()
  }

  private fun call(): Expr {
    var expr = primary()

    while (true) {
      if (match(LEFT_PAREN)) {
        expr = finishCall(expr)
      } else {
        break
      }
    }

    return expr
  }

  private fun finishCall(callee: Expr): Expr {
    val arguments: MutableList<Expr> = mutableListOf()

    if (!check(RIGHT_PAREN)) {
      do {
        if (arguments.size >= 255) {
          error(peek(), "Cannot have more than 255 arguments.")
        }
        arguments.add(expression())
      } while (match(COMMA))
      // TODO: this doesn't handle trailing commas
    }

    val paren = consume(RIGHT_PAREN, "Expect ')' after arguments.")

    return Call(callee, paren, arguments)
  }

  private fun primary(): Expr {
    logger.debug { "Parsing primary: " + peek().toString() }
    if (match(FALSE)) return Literal(false)
    if (match(TRUE)) return Literal(true)
    if (match(NIL)) return Literal(null)

    if (match(NUMBER, STRING)) {
      return Literal(previous().literal)
    }

    if (match(IDENTIFIER)) {
      return Variable(previous())
    }

    if (match(LEFT_PAREN)) {
      val expr = expression()
      consume(RIGHT_PAREN, "Expect ')' after expression.")
      return Grouping(expr)
    }

    throw error(peek(), "Expect expression")
  }

  // Helpers

  private fun leftAssociativeBinary(types: Array<TokenType>, childParser: () -> Expr): Expr {
    var expr = childParser()

    while (match(*types)) {
      val op = previous()
      val right = childParser()
      expr = Binary(expr, op, right)
    }

    return expr
  }

  private fun match(vararg types: TokenType): Boolean {
    for (type in types) {
      if (check(type)) {
        advance()
        return true
      }
    }
    return false
  }

  private fun check(type: TokenType): Boolean {
    if (isAtEnd()) return false
    return peek().type == type
  }

  private fun advance(): Token {
    if (!isAtEnd()) current++
    return previous()
  }

  private fun isAtEnd(): Boolean {
    return peek().type == EOF
  }

  private fun peek(): Token {
    return tokens[current]
  }

  private fun previous(): Token {
    return tokens[current - 1]
  }

  private fun consume(type: TokenType, message: String): Token {
    if (check(type)) return advance()
    throw error(peek(), message)
  }

  // Error handling

  private fun error(token: Token, message: String): ParseError {
    loxError(token, message)
    return ParseError()
  }

  private fun synchronize() {
    advance()
    while (!isAtEnd()) {
      if (previous().type == SEMICOLON) return

      when (peek().type) {
        CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> return
        else -> advance()
      }
    }
  }
}
