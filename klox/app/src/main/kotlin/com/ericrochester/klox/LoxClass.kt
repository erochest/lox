package com.ericrochester.klox

data class LoxClass(val name: String) : LoxCallable {
  override fun toString(): String = name

  override fun arity(): Int = 0

  override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
    val instance = LoxInstance(this)
    return instance
  }
}
