package com.ericrochester.klox

data class LoxClass(val name: String, val methods: Map<String, LoxFunction>) : LoxCallable {
  override fun toString(): String = name

  override fun arity(): Int {
    val initializer = findMethod("init")
    return initializer?.arity() ?: 0
  }

  override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
    val instance = LoxInstance(this)
    val initializer = findMethod("init")

    initializer?.bind(instance)?.call(interpreter, arguments)

    return instance
  }

  fun findMethod(name: String): LoxFunction? {
    return methods[name]
  }
}
