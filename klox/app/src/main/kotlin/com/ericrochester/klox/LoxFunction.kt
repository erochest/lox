package com.ericrochester.klox

class LoxFunction(val declaration: Function, val closure: Environment) : LoxCallable {
  override fun arity(): Int = declaration.params.size

  override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
    val environment = Environment(closure)
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
