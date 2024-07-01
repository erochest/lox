package com.ericrochester.klox

import com.ericrochester.klox.app.runtimeError

// TODO: Support casting the other value to a string if one of the operands of + is a string
// TODO: runtime error for divid by zero

class Interpreter: ExprVisitor<Any?> {

    fun interpret(expr: Expr) {
        try {
            val result = evaluate(expr)
            println(stringify(result))
        } catch (error: RuntimeError) {
            runtimeError(error)
        }
    }

    override fun visitLiteralExpr(literal: Literal): Any? {
        return literal.value
    }

    override fun visitGroupingExpr(grouping: Grouping): Any? {
        return evaluate(grouping.expression)
    }

    override fun visitUnaryExpr(unary: Unary): Any? {
        val right = evaluate(unary.right)
        return when (unary.operator.type) {
            TokenType.MINUS -> {
                checkNumberOperand(unary.operator, right)
                -1 * (right as Double)
            }
            TokenType.BANG -> ! isTruthy(right)
            else -> throw RuntimeError(unary.operator, "Unknown unary operator: ${unary.operator.lexeme}")
        }
    }

    override fun visitBinaryExpr(binary: Binary): Any? {
        val left = evaluate(binary.left)
        val right = evaluate(binary.right)

        return when (binary.operator.type) {
            TokenType.MINUS -> {
                checkNumberOperands(binary.operator, left, right)
                left as Double - right as Double
            }
            TokenType.PLUS -> if (left is Double && right is Double) left + right
                              else if (left is String && right is String) left + right
                              else throw RuntimeError(binary.operator, "Operands must be two numbers or two strings: ${binary.operator.lexeme}")
            TokenType.SLASH -> {
                checkNumberOperands(binary.operator, left, right)
                left as Double / right as Double
            }
            TokenType.STAR -> {
                checkNumberOperands(binary.operator, left, right)
                left as Double * right as Double
            }
            TokenType.GREATER -> {
                checkNumberOperands(binary.operator, left, right)
                left as Double > right as Double
            }
            TokenType.GREATER_EQUAL -> {
                checkNumberOperands(binary.operator, left, right)
                left as Double >= right as Double
            }
            TokenType.LESS -> {
                checkNumberOperands(binary.operator, left, right)
                (left as Double) < (right as Double)
            }
            TokenType.LESS_EQUAL -> {
                checkNumberOperands(binary.operator, left, right)
                left as Double <= right as Double
            }
            TokenType.EQUAL_EQUAL -> isEqual(left, right)
            TokenType.BANG_EQUAL -> !isEqual(left, right)
            else -> throw RuntimeError(binary.operator, "Unknown binary operator: ${binary.operator.lexeme}")
        }
    }

    override fun visitTernaryExpr(ternary: Ternary): Any? {
        val condition = evaluate(ternary.condition)
        return if (isTruthy(condition)) evaluate(ternary.then) else evaluate(ternary.alternative)
    }

    private fun evaluate(expr: Expr): Any? {
        return expr.accept(this)
    }

    private fun isTruthy(value: Any?): Boolean {
        if (value == null) return false
        if (value is Boolean) return value

        return true
    }

    private fun isEqual(left: Any?, right: Any?): Boolean {
        if (left == null && right == null) return true
        if (left == null) return false

        return left == right
    }

    private fun checkNumberOperand(operator: Token, operand: Any?) {
        if (operand is Double) return
        throw RuntimeError(operator, "Operand must be a number: ${operator.lexeme}")
    }

    private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
        if (left is Double && right is Double) return
        throw RuntimeError(operator, "Operands must be numbers: ${operator.lexeme}")
    }

    private fun stringify(value: Any?): String {
        if (value == null) return "nil"

        if (value is Double) {
            val text = value.toString()
            if (text.endsWith(".0")) {
                return text.substring(0, text.length - 2)
            }
            return text
        }
        
        return value.toString()
    }
}