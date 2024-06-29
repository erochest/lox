package com.ericrochester.klox

import com.ericrochester.klox.TokenType.*
import com.ericrochester.klox.app.error as loxError

// TODO: Add a comma operator between expressions
// TODO: Add a ternary operator
// TODO: Detect and handle the error of a binary operator occurring at the beginning of an expression (missing left-hand side)


// The parser so far.
// expression     → equality ;
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

    fun parse(): Expr? {
        try {
            return expression()
        } catch (e: ParseError) {
            return null
        }
    }

    // Productions

    private fun expression(): Expr {
        return equality();
    }

    private fun equality(): Expr {
        return leftAssociativeBinary(arrayOf(BANG_EQUAL, EQUAL_EQUAL)) { comparison() }
    }

    private fun comparison(): Expr {
        return leftAssociativeBinary(arrayOf(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) { term() }
    }

    private fun term(): Expr {
        return leftAssociativeBinary(arrayOf(MINUS, PLUS)) { factor() }
    }

    private fun factor(): Expr {
        return leftAssociativeBinary(arrayOf(SLASH, STAR)) { unary() }
    }

    private fun unary(): Expr {
        if (match(BANG, MINUS)) {
            val op = previous()
            val right = unary()
            return Unary(op, right)
        }
        return primary()
    }

    private fun primary(): Expr {
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