package com.ericrochester.klox

class AstPrinter : ExprVisitor<String> {
    fun print(expr: Expr): String {
        return expr.accept(this)
    }

    override fun visitBinaryExpr(binary: Binary): String {
        return parenthesize(binary.operator.lexeme, binary.left, binary.right)
    }

    override fun visitGroupingExpr(grouping: Grouping): String {
        return parenthesize("group", grouping.expression)
    }

    override fun visitLiteralExpr(literal: Literal): String {
        return literal.value?.toString() ?: "nil"
    }

    override fun visitUnaryExpr(unary: Unary): String {
        return parenthesize(unary.operator.lexeme, unary.right)
    }

    private fun parenthesize(name: String, vararg exprs: Expr): String {
        val builder = StringBuilder()
        builder.append("(").append(name)
        exprs.forEach { builder.append(" ").append(it.accept(this)) }
        builder.append(")")
        return builder.toString()
    }
}