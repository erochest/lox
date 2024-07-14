package com.ericrochester.klox

class LoxFunction(val declaration: Function, val closure: Environment, val isInitializer: Boolean) : LoxCallable {
  override fun arity(): Int = declaration.params.size

  override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
    val environment = Environment(closure)
    for ((index, param) in declaration.params.withIndex()) {
      environment.define(param.lexeme, arguments[index])
    }

    try {
      interpreter.executeBlock(declaration.body, environment)
    } catch (returnValue: Return) {
      if (isInitializer) return closure.getAt(0, "this")
      return returnValue.value
    }

    if (isInitializer) return closure.getAt(0, "this")
    return null
  }

  fun bind(instance: LoxInstance): LoxFunction {
    val environment = Environment(closure)
    environment.define("this", instance)
    return LoxFunction(declaration, environment, isInitializer)
  }

  override fun toString(): String = "<fn ${declaration.name.lexeme}>"
}
