package com.ericrochester.klox

import com.ericrochester.klox.app.error as loxError
import kotlin.collections.ArrayDeque

class Resolver(val interpreter: Interpreter) : ExprVisitor<Unit>, StmtVisitor<Unit> {

  private enum class FunctionType {
    NONE,
    FUNCTION,
    INITIALIZER,
    METHOD,
  }

  private enum class ClassType {
    NONE,
    CLASS,
    SUBCLASS,
  }

  private val scopes = ArrayDeque<MutableMap<String, Boolean>>()
  private var currentFunctionType = FunctionType.NONE
  private var currentClassType = ClassType.NONE

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
    if (scope.containsKey(name.lexeme)) {
      loxError(name, "Already a variable with this name in this scope.")
    }
    scope[name.lexeme] = false
  }

  private fun define(name: Token) {
    if (scopes.isEmpty()) return
    val scope = scopes.last()
    scope[name.lexeme] = true
  }

  private fun resolveFunction(function: Function, type: FunctionType) {
    val enclosingFunctionType = currentFunctionType
    currentFunctionType = type

    beginScope()
    function.params.forEach {
      declare(it)
      define(it)
    }
    resolve(function.body)
    endScope()
    currentFunctionType = enclosingFunctionType
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

  override fun visitClassStmtStmt(classstmtStmt: ClassStmt) {
    val enclosingClassType = currentClassType
    currentClassType = ClassType.CLASS

    declare(classstmtStmt.name)
    define(classstmtStmt.name)

    classstmtStmt.superclass?.let {
      currentClassType = ClassType.SUBCLASS
      if (classstmtStmt.name.lexeme == it.name.lexeme) {
        loxError(it.name, "A class can't inherit from itself.")
      }
      // currentClassType = ClassType.CLASS
      resolve(it)

      beginScope()
      scopes.last()["super"] = true
    }

    beginScope()
    scopes.last()["this"] = true

    classstmtStmt.methods.forEach {
      val declaration = if (it.name.lexeme == "init") FunctionType.INITIALIZER else FunctionType.METHOD
      resolveFunction(it, declaration)
    }

    endScope()
    classstmtStmt.superclass?.let { endScope() }
    currentClassType = enclosingClassType
  }

  override fun visitFunctionStmt(functionStmt: Function) {
    declare(functionStmt.name)
    define(functionStmt.name)

    resolveFunction(functionStmt, FunctionType.FUNCTION)
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
    ifStmt.elseBranch?.let { resolve(it) }
  }

  override fun visitPrintStmt(printStmt: Print) {
    resolve(printStmt.expression)
  }

  override fun visitReturnStmtStmt(returnstmtStmt: ReturnStmt) {
    if (currentFunctionType == FunctionType.NONE) {
      loxError(returnstmtStmt.keyword, "Can't return from top-level code.")
    }
    if (returnstmtStmt.value != null) {
      if (currentFunctionType == FunctionType.INITIALIZER) {
        loxError(returnstmtStmt.keyword, "Can't return a value from an initializer.")
      }
      resolve(returnstmtStmt.value)
    }
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
    if (!scopes.isEmpty() && scopes.last()[variableExpr.name.lexeme] == false) {
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

  override fun visitCommaExpr(commaExpr: Comma) {
    commaExpr.expressions.forEach { resolve(it) }
  }

  override fun visitGetExpr(getExpr: Get) {
    resolve(getExpr.obj)
  }

  override fun visitGroupingExpr(groupingExpr: Grouping) {
    resolve(groupingExpr.expression)
  }

  override fun visitLiteralExpr(literalExpr: Literal) {}

  override fun visitLogicalExpr(logicalExpr: Logical) {
    resolve(logicalExpr.left)
    resolve(logicalExpr.right)
  }

  override fun visitSetExpr(setExpr: Set) {
    resolve(setExpr.value)
    resolve(setExpr.obj)
  }

  override fun visitSuperExpr(superExpr: Super) {
    if (currentClassType == ClassType.NONE) {
      loxError(superExpr.keyword, "Can't use 'super' outside of a class.")
    } else if (currentClassType != ClassType.SUBCLASS) {
      loxError(superExpr.keyword, "Can't use 'super' in a class with no superclass.")
    }

    resolveLocal(superExpr, superExpr.keyword)
  }

  override fun visitTernaryExpr(ternaryExpr: Ternary) {
    resolve(ternaryExpr.condition)
    resolve(ternaryExpr.thenBranch)
    resolve(ternaryExpr.elseBranch)
  }

  override fun visitThisExpr(thisExpr: This) {
    if (currentClassType == ClassType.NONE) {
      loxError(thisExpr.keyword, "Can't use 'this' outside of a class.")
      return
    }
    resolveLocal(thisExpr, thisExpr.keyword)
  }

  override fun visitUnaryExpr(unaryExpr: Unary) {
    resolve(unaryExpr.right)
  }
}
