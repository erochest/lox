package com.ericrochester.klox

import com.ericrochester.klox.app.runtimeError

class Interpreter : ExprVisitor<Any?>, StmtVisitor<Unit> {
  var environment = Environment()

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

  override fun visitTernaryExpr(ternaryExpr: Ternary): Any? {
    val condition = evaluate(ternaryExpr.condition)
    return if (isTruthy(condition)) evaluate(ternaryExpr.thenBranch)
    else evaluate(ternaryExpr.elseBranch)
  }

  override fun visitVariableExpr(variableExpr: Variable): Any? {
    return environment.get(variableExpr.name)
  }

  // Statement visitor

  override fun visitExpressionStmt(expressionStmt: Expression) {
    evaluate(expressionStmt.expression)
  }

  override fun visitPrintStmt(printStmt: Print) {
    val value = evaluate(printStmt.expression)
    println(stringify(value))
  }

  override fun visitVarStmt(varStmt: Var) {
    val value = varStmt.initializer?.let { evaluate(it) }
    environment.define(varStmt.name.lexeme, value)
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

  private fun executeBlock(statements: List<Stmt?>, environment: Environment) {
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
