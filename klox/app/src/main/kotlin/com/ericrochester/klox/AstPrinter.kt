package com.ericrochester.klox

class AstPrinter : ExprVisitor<String> {
  fun print(expr: Expr): String {
    return expr.accept(this)
  }

  override fun visitBinaryExpr(binaryExpr: Binary): String {
    return rpn(binaryExpr.left, binaryExpr.right, binaryExpr.operator.lexeme)
  }

  override fun visitCallExpr(callExpr: Call): String {
    val buffer = StringBuffer()

    buffer.append(callExpr.arguments.joinToString(" ") { it.accept(this) })
    buffer.append(" ")
    buffer.append(callExpr.callee.accept(this))

    return buffer.toString()
  }

  override fun visitGetExpr(getExpr: Get): String {
    return rpn(getExpr.obj, getExpr.name.lexeme, '.')
  }

  override fun visitGroupingExpr(groupingExpr: Grouping): String {
    return groupingExpr.expression.accept(this) // groupingExpr does not affect RPN notation
  }

  override fun visitLiteralExpr(literalExpr: Literal): String {
    return literalExpr.value?.toString() ?: "nil"
  }

  override fun visitLogicalExpr(logicalExpr: Logical): String {
    return rpn(logicalExpr.left, logicalExpr.right, logicalExpr.operator.lexeme)
  }

  override fun visitSetExpr(setExpr: Set): String {
    return rpn(setExpr.obj, setExpr.name.lexeme, setExpr.value, '=')
  }

  override fun visitUnaryExpr(unaryExpr: Unary): String {
    return rpn(
        unaryExpr.right,
        unaryExpr.operator.lexeme
    ) // Unary operations in RPN place the operator after the operand
  }

  override fun visitAssignExpr(assignExpr: Assign): String {
    return rpn(assignExpr.value, assignExpr.name.lexeme, "=")
  }

  override fun visitVariableExpr(variableExpr: Variable): String {
    return variableExpr.name.lexeme
  }

  private fun rpn(vararg parts: Any): String {
    return parts.joinToString(" ") { part ->
      when (part) {
        is Expr -> part.accept(this)
        else -> part.toString()
      }
    }
  }
}
