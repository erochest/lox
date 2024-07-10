package com.ericrochester.klox

import com.ericrochester.klox.app.runtimeError

class Interpreter : ExprVisitor<Any?>, StmtVisitor<Unit> {
  val globals = Environment()
  var environment = globals

  init {
    globals.define(
        "clock",
        object : LoxCallable {
          override fun arity(): Int = 0
          override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? =
              System.currentTimeMillis().toDouble() / 1000.0
          override fun toString(): String = "<native fun>"
        }
    )
  }

  fun interpret(statements: List<Stmt?>) {
    try {
      for (statement in statements) {
        statement?.let { execute(it) }
      }
    } catch (error: RuntimeError) {
      runtimeError(error)
    }
  }

  // Expression visitor

  override fun visitLiteralExpr(literalExpr: Literal): Any? {
    return literalExpr.value
  }

  override fun visitLogicalExpr(logicalExpr: Logical): Any? {
    val left = evaluate(logicalExpr.left)
    if (logicalExpr.operator.type == TokenType.OR) {
      if (isTruthy(left)) return left
    } else {
      if (!isTruthy(left)) return left
    }
    return evaluate(logicalExpr.right)
  }

  override fun visitGroupingExpr(groupingExpr: Grouping): Any? {
    return evaluate(groupingExpr.expression)
  }

  override fun visitUnaryExpr(unaryExpr: Unary): Any? {
    val right = evaluate(unaryExpr.right)
    return when (unaryExpr.operator.type) {
      TokenType.MINUS -> {
        checkNumberOperand(unaryExpr.operator, right)
        -1 * (right as Double)
      }
      TokenType.BANG -> !isTruthy(right)
      else ->
          throw RuntimeError(
              unaryExpr.operator,
              "Unknown unary operator: ${unaryExpr.operator.lexeme}"
          )
    }
  }

  override fun visitBinaryExpr(binaryExpr: Binary): Any? {
    val left = evaluate(binaryExpr.left)
    val right = evaluate(binaryExpr.right)

    return when (binaryExpr.operator.type) {
      TokenType.MINUS -> {
        checkNumberOperands(binaryExpr.operator, left, right)
        left as Double - right as Double
      }
      TokenType.PLUS ->
          if (left is Double && right is Double) left + right
          else if (left is String && right is String) left + right
          else if (left is String) left + stringify(right)
          else if (right is String) stringify(left) + right
          else
              throw RuntimeError(
                  binaryExpr.operator,
                  "Operands must be two numbers or two strings: ${binaryExpr.operator.lexeme}"
              )
      TokenType.SLASH -> {
        checkNumberOperands(binaryExpr.operator, left, right)
        if (right as Double == 0.0) throw RuntimeError(binaryExpr.operator, "Division by zero")
        left as Double / right
      }
      TokenType.STAR -> {
        checkNumberOperands(binaryExpr.operator, left, right)
        left as Double * right as Double
      }
      TokenType.GREATER -> {
        checkNumberOperands(binaryExpr.operator, left, right)
        left as Double > right as Double
      }
      TokenType.GREATER_EQUAL -> {
        checkNumberOperands(binaryExpr.operator, left, right)
        left as Double >= right as Double
      }
      TokenType.LESS -> {
        checkNumberOperands(binaryExpr.operator, left, right)
        (left as Double) < (right as Double)
      }
      TokenType.LESS_EQUAL -> {
        checkNumberOperands(binaryExpr.operator, left, right)
        left as Double <= right as Double
      }
      TokenType.EQUAL_EQUAL -> isEqual(left, right)
      TokenType.BANG_EQUAL -> !isEqual(left, right)
      else ->
          throw RuntimeError(
              binaryExpr.operator,
              "Unknown binary operator: ${binaryExpr.operator.lexeme}"
          )
    }
  }

  override fun visitCallExpr(callExpr: Call): Any? {
    val callee = evaluate(callExpr.callee)
    val arguments = callExpr.arguments.map { evaluate(it) }

    val function =
        callee as? LoxCallable
            ?: throw RuntimeError(callExpr.paren, "Can only call functions and classes.")
    if (arguments.size != function.arity())
        throw RuntimeError(
            callExpr.paren,
            "Expected ${function.arity()} arguments but got ${arguments.size}."
        )

    return function.call(this, arguments)
  }

  override fun visitVariableExpr(variableExpr: Variable): Any? {
    return environment.get(variableExpr.name)
  }

  // Statement visitor

  override fun visitExpressionStmt(expressionStmt: Expression) {
    evaluate(expressionStmt.expression)
  }

  override fun visitFunctionStmt(functionStmt: Function) {
    val function = LoxFunction(functionStmt, environment)
    environment.define(functionStmt.name.lexeme, function)
  }

  override fun visitIfStmt(ifStmt: If) {
    if (isTruthy(evaluate(ifStmt.condition))) {
      execute(ifStmt.thenBranch)
    } else {
      ifStmt.elseBranch?.let { execute(it) }
    }
  }

  override fun visitPrintStmt(printStmt: Print) {
    val value = evaluate(printStmt.expression)
    println(stringify(value))
  }

  override fun visitReturnStmtStmt(returnstmtStmt: ReturnStmt) {
    val value = returnstmtStmt.value?.let { evaluate(it) }
    throw Return(value)
  }

  override fun visitVarStmt(varStmt: Var) {
    val value = varStmt.initializer?.let { evaluate(it) }
    environment.define(varStmt.name.lexeme, value)
  }

  override fun visitWhileStmt(whileStmt: While) {
    // TODO: break statement
    while (isTruthy(evaluate(whileStmt.condition))) {
      execute(whileStmt.body)
    }
  }

  override fun visitAssignExpr(assignExpr: Assign): Any? {
    val value = evaluate(assignExpr.value)
    environment.assign(assignExpr.name, value)
    return value
  }

  override fun visitBlockStmt(blockStmt: Block) {
    executeBlock(blockStmt.statements, Environment(environment))
  }

  // Helpers

  private fun execute(stmt: Stmt) {
    stmt.accept(this)
  }

  fun executeBlock(statements: List<Stmt?>, environment: Environment) {
    val previous = this.environment

    try {
      this.environment = environment

      for (statement in statements) {
        statement?.let { execute(it) }
      }
    } finally {
      this.environment = previous
    }
  }

  private fun evaluate(expr: Expr): Any? {
    return expr.accept(this)
  }

  private fun isTruthy(value: Any?): Boolean {
    if (value == null) return false
    if (value is Boolean) return value

    return true
  }

  private fun isEqual(left: Any?, right: Any?): Boolean {
    if (left == null && right == null) return true
    if (left == null) return false

    return left == right
  }

  private fun checkNumberOperand(operator: Token, operand: Any?) {
    if (operand is Double) return
    throw RuntimeError(operator, "Operand must be a number: ${operator.lexeme}")
  }

  private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
    if (left is Double && right is Double) return
    throw RuntimeError(operator, "Operands must be numbers: ${operator.lexeme}")
  }

  private fun stringify(value: Any?): String {
    if (value == null) return "nil"

    if (value is Double) {
      val text = value.toString()
      if (text.endsWith(".0")) {
        return text.substring(0, text.length - 2)
      }
      return text
    }

    return value.toString()
  }
}
