package com.ericrochester.klox

import com.ericrochester.klox.app.error as loxError
import com.ericrochester.klox.TokenType.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

// The parser so far.
// program        → statement* EOF ;
//
// statement      → exprStmt
//                | printStmt ;
//
// exprStmt       → expression ";" ;
// printStmt      → "print" expression ";" ;
// expression     → ternary ( "," ternary )*
//                | ( "," | "?" | "!=" | "==" | ">" | ">=" | "<" | "<=" | "+" | "/" | "*" ) ;
// ternary        → equality ( "?" expression ":" expression )? ;
// equality       → comparison ( ( "!=" | "==" ) comparison )* ;
// comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
// term           → factor ( ( "-" | "+" ) factor )* ;
// factor         → unary ( ( "/" | "*" ) unary )* ;
// unary          → ( "!" | "-" ) unary
//                | primary ;
// primary        → NUMBER | STRING | "true" | "false" | "nil"
//                | "(" expression ")" ;

class Parser(private val tokens: List<Token>) {
    private class ParseError: RuntimeException()

    private var current = 0

    // Interface

    fun parse(): List<Stmt> {
        logger.debug { "Starting parse: " + peek().toString() }
        val statements: MutableList<Stmt> = mutableListOf()
        while (!isAtEnd()) {
          statements.add(statement())
        }

        return statements
    }

    // Productions
    private fun statement(): Stmt {
      if (match(PRINT)) return printStatement()
      return expressionStatement()
    }

    private fun printStatement(): Stmt {
      val value = expression()
      consume(SEMICOLON, "Expect ';' after value.")
      return Print(value)
    }

    private fun expressionStatement(): Stmt {
      val value = expression();
      consume(SEMICOLON, "Expect ';' after expression.")
      return Expression(value)
    }

    private fun expression(): Expr {
        logger.debug { "Parsing expression: " + peek().toString() }

        if (match(COMMA, QUESTION, BANG_EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL, PLUS, SLASH, STAR)) {
            throw error(previous(), "Unexpected binary operator at start of expression.")
        }
        return leftAssociativeBinary(arrayOf(COMMA)) { ternary() }
    }

    private fun ternary(): Expr {
        logger.debug { "Parsing ternary: " + peek().toString() }
        val expr = equality()

        if (match(QUESTION)) {
            val then = expression()
            consume(COLON, "Expect ':' after '?'.")
            val alternative = expression()
            return Ternary(expr, then, alternative)
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
        return primary()
    }

    private fun primary(): Expr {
        logger.debug { "Parsing primary: " + peek().toString() }
        if (match(FALSE)) return Literal(false)
        if (match(TRUE)) return Literal(true)
        if (match(NIL)) return Literal(null)

        if (match(NUMBER, STRING)) {
            return Literal(previous().literal)
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
