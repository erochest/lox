package com.ericrochester.klox

import com.ericrochester.klox.Token

abstract class Expr {
    abstract fun <R> accept(visitor: ExprVisitor<R>): R
}

interface ExprVisitor<R> {
    fun visitAssignExpr(assignExpr: Assign): R
    fun visitBinaryExpr(binaryExpr: Binary): R
    fun visitCallExpr(callExpr: Call): R
    fun visitGetExpr(getExpr: Get): R
    fun visitGroupingExpr(groupingExpr: Grouping): R
    fun visitLiteralExpr(literalExpr: Literal): R
    fun visitLogicalExpr(logicalExpr: Logical): R
    fun visitSetExpr(setExpr: Set): R
    fun visitThisExpr(thisExpr: This): R
    fun visitUnaryExpr(unaryExpr: Unary): R
    fun visitVariableExpr(variableExpr: Variable): R
}

data class Assign(
    val name: Token,
    val value: Expr,
) : Expr() {
    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return visitor.visitAssignExpr(this)
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

data class Call(
    val callee: Expr,
    val paren: Token,
    val arguments: List<Expr>,
) : Expr() {
    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return visitor.visitCallExpr(this)
    }
}

data class Get(
    val obj: Expr,
    val name: Token,
) : Expr() {
    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return visitor.visitGetExpr(this)
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

data class Logical(
    val left: Expr,
    val operator: Token,
    val right: Expr,
) : Expr() {
    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return visitor.visitLogicalExpr(this)
    }
}

data class Set(
    val obj: Expr,
    val name: Token,
    val value: Expr,
) : Expr() {
    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return visitor.visitSetExpr(this)
    }
}

data class This(
    val keyword: Token,
) : Expr() {
    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return visitor.visitThisExpr(this)
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

