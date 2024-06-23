package com.ericrochester.klox

class AstPrinter : ExprVisitor<String> {
    fun print(expr: Expr): String {
        return expr.accept(this)
    }

    override fun visitBinaryExpr(binary: Binary): String {
        return rpn(binary.left, binary.right, binary.operator.lexeme)
    }

    override fun visitGroupingExpr(grouping: Grouping): String {
        return grouping.expression.accept(this) // Grouping does not affect RPN notation
    }

    override fun visitLiteralExpr(literal: Literal): String {
        return literal.value?.toString() ?: "nil"
    }

    override fun visitUnaryExpr(unary: Unary): String {
        return rpn(unary.right, unary.operator.lexeme) // Unary operations in RPN place the operator after the operand
    }

    private fun rpn(vararg parts: Any): String {
        return parts.joinToString(" ") { part ->
            when (part) {
                is Expr -> part.accept(this)
                else -> part.toString()
            }
        }
    }
}
