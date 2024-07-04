package com.ericrochester.klox

import com.ericrochester.klox.Token

abstract class Expr {
    abstract fun <R> accept(visitor: ExprVisitor<R>): R
}

interface ExprVisitor<R> {
    fun visitTernaryExpr(ternaryExpr: Ternary): R
    fun visitBinaryExpr(binaryExpr: Binary): R
    fun visitGroupingExpr(groupingExpr: Grouping): R
    fun visitLiteralExpr(literalExpr: Literal): R
    fun visitUnaryExpr(unaryExpr: Unary): R
    fun visitVariableExpr(variableExpr: Variable): R
}

data class Ternary(
    val condition: Expr,
    val thenBranch: Expr,
    val elseBranch: Expr,
) : Expr() {
    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return visitor.visitTernaryExpr(this)
    }
}

data class Binary(
    val left: Expr,
    val operator: Token,
    val right: Expr,
) : Expr() {
    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return visitor.visitBinaryExpr(this)
    }
}

data class Grouping(
    val expression: Expr,
) : Expr() {
    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return visitor.visitGroupingExpr(this)
    }
}

data class Literal(
    val value: Any?,
) : Expr() {
    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return visitor.visitLiteralExpr(this)
    }
}

data class Unary(
    val operator: Token,
    val right: Expr,
) : Expr() {
    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return visitor.visitUnaryExpr(this)
    }
}

data class Variable(
    val name: Token,
) : Expr() {
    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return visitor.visitVariableExpr(this)
    }
}

