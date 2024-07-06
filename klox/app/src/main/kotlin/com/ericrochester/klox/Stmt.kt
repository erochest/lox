package com.ericrochester.klox

import com.ericrochester.klox.Token

abstract class Stmt {
    abstract fun <R> accept(visitor: StmtVisitor<R>): R
}

interface StmtVisitor<R> {
    fun visitBlockStmt(blockStmt: Block): R
    fun visitExpressionStmt(expressionStmt: Expression): R
    fun visitIfStmt(ifStmt: If): R
    fun visitPrintStmt(printStmt: Print): R
    fun visitVarStmt(varStmt: Var): R
    fun visitWhileStmt(whileStmt: While): R
}

data class Block(
    val statements: List<Stmt?>,
) : Stmt() {
    override fun <R> accept(visitor: StmtVisitor<R>): R {
        return visitor.visitBlockStmt(this)
    }
}

data class Expression(
    val expression: Expr,
) : Stmt() {
    override fun <R> accept(visitor: StmtVisitor<R>): R {
        return visitor.visitExpressionStmt(this)
    }
}

data class If(
    val condition: Expr,
    val thenBranch: Stmt,
    val elseBranch: Stmt?,
) : Stmt() {
    override fun <R> accept(visitor: StmtVisitor<R>): R {
        return visitor.visitIfStmt(this)
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

data class While(
    val condition: Expr,
    val body: Stmt,
) : Stmt() {
    override fun <R> accept(visitor: StmtVisitor<R>): R {
        return visitor.visitWhileStmt(this)
    }
}

