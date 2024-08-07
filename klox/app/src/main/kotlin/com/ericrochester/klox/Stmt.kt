package com.ericrochester.klox

import com.ericrochester.klox.Token

abstract class Stmt {
    abstract fun <R> accept(visitor: StmtVisitor<R>): R
}

interface StmtVisitor<R> {
    fun visitBlockStmt(blockStmt: Block): R
    fun visitClassStmtStmt(classstmtStmt: ClassStmt): R
    fun visitExpressionStmt(expressionStmt: Expression): R
    fun visitFunctionStmt(functionStmt: Function): R
    fun visitIfStmt(ifStmt: If): R
    fun visitPrintStmt(printStmt: Print): R
    fun visitReturnStmtStmt(returnstmtStmt: ReturnStmt): R
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

data class ClassStmt(
    val name: Token,
    val superclass: Variable?,
    val methods: List<Function>,
) : Stmt() {
    override fun <R> accept(visitor: StmtVisitor<R>): R {
        return visitor.visitClassStmtStmt(this)
    }
}

data class Expression(
    val expression: Expr,
) : Stmt() {
    override fun <R> accept(visitor: StmtVisitor<R>): R {
        return visitor.visitExpressionStmt(this)
    }
}

data class Function(
    val name: Token,
    val params: List<Token>,
    val body: List<Stmt>,
) : Stmt() {
    override fun <R> accept(visitor: StmtVisitor<R>): R {
        return visitor.visitFunctionStmt(this)
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

data class ReturnStmt(
    val keyword: Token,
    val value: Expr?,
) : Stmt() {
    override fun <R> accept(visitor: StmtVisitor<R>): R {
        return visitor.visitReturnStmtStmt(this)
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

