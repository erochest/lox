package com.ericrochester.klox

import kotlin.collections.ArrayDeque

import com.ericrochester.klox.app.error as loxError

class Resolver(val interpreter: Interpreter) : ExprVisitor<Unit>, StmtVisitor<Unit> {
  
  private val scopes = ArrayDeque<MutableMap<String, Boolean>>()

  // Helpers

  fun resolve(statements: List<Stmt>) {
    statements.forEach { resolve(it) }
  }

  fun resolve(stmt: Stmt) {
    stmt.accept(this)
  }

  fun resolve(expr: Expr) {
    expr.accept(this)
  }

  private fun beginScope() {
    scopes.add(mutableMapOf())
  }

  private fun endScope() {
    scopes.removeLast()
  }

  private fun declare(name: Token) {
    if (scopes.isEmpty()) return
    val scope = scopes.last()
    scope[name.lexeme] = false
  }

  private fun define(name: Token) {
    if (scopes.isEmpty()) return
    val scope = scopes.last()
    scope[name.lexeme] = true
  }

  private fun resolveFunction(function: Function) {
    beginScope()
    function.params.forEach {
      declare(it)
      define(it)
    }
    resolve(function.body)
    endScope()
  }

  private fun resolveLocal(expr: Expr, name: Token) {
    for (i in (scopes.size - 1) downTo 0) {
      if (scopes[i].containsKey(name.lexeme)) {
        interpreter.resolve(expr, scopes.size - 1 - i)
        return
      }
    }
  }

  // Statement visitor

  override fun visitBlockStmt(blockStmt: Block) {
    beginScope()
    resolve(blockStmt.statements.filterNotNull())
    endScope()
  }

  override fun visitFunctionStmt(functionStmt: Function) {
    declare(functionStmt.name)
    define(functionStmt.name)
    
    resolveFunction(functionStmt)
  }

  override fun visitVarStmt(varStmt: Var) {
    declare(varStmt.name)
    varStmt.initializer?.let { resolve(it) }
    define(varStmt.name)
  }

  override fun visitExpressionStmt(expressionStmt: Expression) {
    resolve(expressionStmt.expression)
  }

  override fun visitIfStmt(ifStmt: If) {
    resolve(ifStmt.condition)
    resolve(ifStmt.thenBranch)
    ifStmt.elseBranch?.let {
      resolve(it)
    }
  }

  override fun visitPrintStmt(printStmt: Print) {
    resolve(printStmt.expression)
  }

  override fun visitReturnStmtStmt(returnStmtStmt: ReturnStmt) {
    returnStmtStmt.value?.let { resolve(it) }
  }

  override fun visitWhileStmt(whileStmt: While) {
    resolve(whileStmt.condition)
    resolve(whileStmt.body)
  }

  // Expression visitor

  override fun visitAssignExpr(assignExpr: Assign) {
    resolve(assignExpr.value)
    resolveLocal(assignExpr, assignExpr.name)
  }

  override fun visitVariableExpr(variableExpr: Variable) {
    if (!scopes.isEmpty() && (scopes.last()[variableExpr.name.lexeme] ?: false) == false) {
      loxError(variableExpr.name, "Can't read local variable in its own initializer.")
    }

    resolveLocal(variableExpr, variableExpr.name)
  }

  override fun visitBinaryExpr(binaryExpr: Binary) {
    resolve(binaryExpr.left)
    resolve(binaryExpr.right)
  }

  override fun visitCallExpr(callExpr: Call) {
    resolve(callExpr.callee)
    callExpr.arguments.forEach { resolve(it) }
  }

  override fun visitGroupingExpr(groupingExpr: Grouping) {
    resolve(groupingExpr.expression)
  }

  override fun visitLiteralExpr(literalExpr: Literal) {
  }

  override fun visitLogicalExpr(logicalExpr: Logical) {
    resolve(logicalExpr.left)
    resolve(logicalExpr.right)
  }

  override fun visitUnaryExpr(unaryExpr: Unary) {
    resolve(unaryExpr.right)
  }

}
