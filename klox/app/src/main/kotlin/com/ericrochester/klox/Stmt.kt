package com.ericrochester.klox

import com.ericrochester.klox.Token

abstract class Stmt {
    abstract fun <R> accept(visitor: StmtVisitor<R>): R
}

interface StmtVisitor<R> {
    fun visitExpressionStmt(expressionStmt: Expression): R
    fun visitPrintStmt(printStmt: Print): R
    fun visitVarStmt(varStmt: Var): R
}

data class Expression(
    val expression: Expr,
) : Stmt() {
    override fun <R> accept(visitor: StmtVisitor<R>): R {
        return visitor.visitExpressionStmt(this)
    }
}

data class Print(
    val expression: Expr,
) : Stmt() {
    override fun <R> accept(visitor: StmtVisitor<R>): R {
        return visitor.visitPrintStmt(this)
    }
}

data class Var(
    val name: Token,
    val initializer: Expr?,
) : Stmt() {
    override fun <R> accept(visitor: StmtVisitor<R>): R {
        return visitor.visitVarStmt(this)
    }
}

