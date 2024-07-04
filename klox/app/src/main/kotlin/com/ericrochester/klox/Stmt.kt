package com.ericrochester.klox

import com.ericrochester.klox.Token

abstract class Stmt {
    abstract fun <R> accept(visitor: StmtVisitor<R>): R
}

interface StmtVisitor<R> {
    fun visitExpressionStmt(expression: Expression): R
    fun visitPrintStmt(print: Print): R
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

