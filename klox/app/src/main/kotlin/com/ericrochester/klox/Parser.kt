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
// classDecl      → "class" IDENTIFIER ( "<" INDENTIFIER )?
//                  "{" function* "}" ;
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
// expression     → assignment ( "," assignment )*
//                | "," | "." | "=" ;
// assignment     → (call "." )? IDENTIFIER "=" assignment
//                | ternary | "?" | ":" ;
// ternary        → logic_or ( "?" expression ":" expression )?
//                | "or" ;
// logic_or       → logic_and ( "or" logic_and )* | "and" ;
// logic_and      → equality ( "and" equality )* | "!=" | "==" ;
// equality       → comparison ( ( "!=" | "==" ) comparison )*
//                | ">" | ">=" | "<" | "<=" ;
// comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* | "+" ;
// term           → factor ( ( "-" | "+" ) factor )* | "/" | "*" ;
// factor         → unary ( ( "/" | "*" ) unary )* ;
// unary          → ( "!" | "-" ) unary
//                | call ;
// call           → primary ( "(" arguments? ")" | "." IDENTIFIER )* ;
// arguments      → assignment ( "," assignment )* ;
// primary        → NUMBER | STRING | "true" | "false" | "nil"
//                | "(" expression ")" | IDENTIFIER
//                | "super" "." IDENTIFIER ;

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

    var superclass: Variable? = null
    if (match(LESS)) {
      consume(IDENTIFIER, "Expect superclass name.")
      superclass = Variable(previous())
    }

    consume(LEFT_BRACE, "Expect '{' before class body.")
    val methods = mutableListOf<Function>()

    while (!check(RIGHT_BRACE) && !isAtEnd()) {
      methods.add(function("method"))
    }

    consume(RIGHT_BRACE, "Expect '}' after class body.")
    return ClassStmt(name, superclass, methods)
  }

  private fun statement(): Stmt? {
    if (match(PRINT)) return printStatement()
    if (match(RETURN)) return returnStatement()
    if (match(WHILE)) return whileStatement()
    if (match(LEFT_BRACE)) return Block(block())
    if (match(IF)) return ifStatement()
    if (match(FOR)) return forStatement()
    return expressionStatement()
  }

  private fun printStatement(): Stmt? {
    val value = expression()
    consume(SEMICOLON, "Expect ';' after value.")
    return value?.let { Print(it) }
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

  private fun whileStatement(): Stmt? {
    consume(LEFT_PAREN, "Expect '(' after 'while'.")
    val condition = expression()
    consume(RIGHT_PAREN, "Expect ')' after condition.")
    val body = statement()

    return if (condition != null && body != null) While(condition, body) else null
  }

  private fun expressionStatement(): Stmt? {
    val value = expression()
    consume(SEMICOLON, "Expect ';' after expression.")
    return value?.let { Expression(it) }
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

  private fun ifStatement(): Stmt? {
    consume(LEFT_PAREN, "Expect '(' after 'if'.")
    val condition = expression()
    consume(RIGHT_PAREN, "Expect ')' after if condition.")

    val thenBranch = statement()
    var elseBranch: Stmt? = null
    if (match(ELSE)) {
      elseBranch = statement()
    }
    return if (condition != null && thenBranch != null && elseBranch != null)
      If(condition, thenBranch, elseBranch)
    else null
  }

  private fun forStatement(): Stmt? {
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
    body = body?.let { While(condition ?: Literal(true), it) }
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

  private fun expression(): Expr? {
    logger.debug { "Parsing expression: " + peek().toString() }

    if (match(DOT, EQUAL, COMMA)) {
      missingLeftError(previous()) { assignment() }
      return null
    }

    val exprList = mutableListOf(assignment())
    while (match(COMMA)) {
      exprList.add(assignment())
    }

    val filtered = exprList.filterNotNull()
    return if (filtered.size == 1) filtered[0] else Comma(filtered)
  }

  private fun assignment(): Expr? {
    if (match(QUESTION, COLON)) {
      missingLeftError(previous()) { expression() }
      return null
    }

    val expr = ternary()

    if (match(EQUAL)) {
      val equals = previous()
      val value = assignment()

      if (expr is Variable) {
        val name = expr.name
        return value?.let { Assign(name, it) }
      } else if (expr is Get) {
        return value?.let { Set(expr.obj, expr.name, it) }
      }
      throw error(equals, "Invalid assignment target.")
    }

    return expr
  }

  private fun ternary(): Expr? {
    if (match(OR)) {
      missingLeftError(previous()) { and() }
      return null
    }

    val condition = or()

    if (match(QUESTION)) {
      val thenBranch = expression()
      consume(COLON, "Expect ':' after ternary then branch.")
      val elseBranch = expression()
      return if (condition != null && thenBranch != null && elseBranch != null)
        Ternary(condition, thenBranch, elseBranch)
      else null
    }

    return condition
  }

  private fun or(): Expr? {
    if (match(AND)) {
      missingLeftError(previous()) { equality() }
      return null
    }

    var expr = and()

    while (match(OR)) {
      val operator = previous()
      val right = and()
      expr = if (expr != null && right != null) Logical(expr, operator, right) else null
    }

    return expr
  }

  private fun and(): Expr? {
    if (match(BANG_EQUAL, EQUAL_EQUAL)) {
      missingLeftError(previous()) { comparison() }
      return null
    }

    var expr = equality()

    while (match(AND)) {
      val operator = previous()
      val right = equality()
      expr = if (expr != null && right != null) Logical(expr, operator, right) else null
    }

    return expr
  }

  private fun equality(): Expr? {
    logger.debug { "Parsing equality: " + peek().toString() }
    if (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
      missingLeftError(previous()) { comparison() }
      return null
    }
    leftAssociativeBinary(arrayOf(BANG_EQUAL, EQUAL_EQUAL)) { comparison() }
    return null
  }

  private fun comparison(): Expr? {
    if (match(PLUS)) {
      missingLeftError(previous()) { term() }
      return null
    }
    logger.debug { "Parsing comparison: " + peek().toString() }
    leftAssociativeBinary(arrayOf(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) { term() }
    return null
  }

  private fun term(): Expr? {
    if (match(SLASH, STAR)) {
      missingLeftError(previous()) { factor() }
      return null
    }
    logger.debug { "Parsing term: " + peek().toString() }
    leftAssociativeBinary(arrayOf(MINUS, PLUS)) { factor() }
    return null
  }

  private fun factor(): Expr? {
    logger.debug { "Parsing factor: " + peek().toString() }
    leftAssociativeBinary(arrayOf(SLASH, STAR)) { unary() }
    return null
  }

  private fun unary(): Expr? {
    logger.debug { "Parsing unary: " + peek().toString() }
    if (match(BANG, MINUS)) {
      val op = previous()
      val right = unary()
      return right?.let { Unary(op, it) }
    }
    return call()
  }

  private fun call(): Expr? {
    var expr = primary()

    while (true) {
      if (match(LEFT_PAREN)) {
        expr = expr?.let { finishCall(it) }
      } else if (match(DOT)) {
        val name = consume(IDENTIFIER, "Expect property name after '.'.")
        expr = expr?.let { Get(it, name) }
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
        assignment()?.let { arguments.add(it) }
      } while (match(COMMA))
      // TODO: this doesn't handle trailing commas
    }

    val paren = consume(RIGHT_PAREN, "Expect ')' after arguments.")

    return Call(callee, paren, arguments)
  }

  private fun primary(): Expr? {
    logger.debug { "Parsing primary: " + peek().toString() }
    if (match(FALSE)) return Literal(false)
    if (match(TRUE)) return Literal(true)
    if (match(NIL)) return Literal(null)

    if (match(NUMBER, STRING)) {
      return Literal(previous().literal)
    }

    if (match(SUPER)) {
      val keyword = previous()
      consume(DOT, "Expect '.' after 'super'.")
      val method = consume(IDENTIFIER, "Expect superclass method name.")
      return Super(keyword, method)
    }

    if (match(THIS)) return This(previous())

    if (match(IDENTIFIER)) {
      return Variable(previous())
    }

    if (match(LEFT_PAREN)) {
      val expr = expression()
      consume(RIGHT_PAREN, "Expect ')' after expression.")
      return expr?.let { Grouping(it) }
    }

    throw error(peek(), "Expect expression")
  }

  // Helpers

  private fun leftAssociativeBinary(types: Array<TokenType>, childParser: () -> Expr?): Expr? {
    var expr = childParser()

    if (expr == null) {
      return null
    }

    while (match(*types)) {
      val op = previous()
      val right = childParser()
      expr = if (expr != null && right != null) Binary(expr, op, right) else null
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

  private fun missingLeftError(operator: Token, next: () -> Expr?) {
    loxError(operator, "Expect expression on the left side of ${operator.lexeme}.")
    next()
  }
}
