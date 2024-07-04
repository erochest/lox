package com.ericrochester.klox

import com.ericrochester.klox.Token

abstract class Expr {
    abstract fun <R> accept(visitor: ExprVisitor<R>): R
}

interface ExprVisitor<R> {
    fun visitBinaryExpr(binary: Binary): R
    fun visitGroupingExpr(grouping: Grouping): R
    fun visitLiteralExpr(literal: Literal): R
    fun visitUnaryExpr(unary: Unary): R
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

