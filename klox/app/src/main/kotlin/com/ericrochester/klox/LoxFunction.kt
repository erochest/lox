package com.ericrochester.klox

class LoxFunction(val declaration: Function) : LoxCallable {
  override fun arity(): Int = declaration.params.size

  override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
    val environment = Environment(interpreter.globals)
    for ((index, param) in declaration.params.withIndex()) {
      environment.define(param.lexeme, arguments[index])
    }

    try {
      interpreter.executeBlock(declaration.body, environment)
    } catch (returnValue: Return) {
      return returnValue.value
    }
    return null
  }

  override fun toString(): String = "<fn ${declaration.name.lexeme}>"
}
