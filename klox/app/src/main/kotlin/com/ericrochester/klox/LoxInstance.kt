package com.ericrochester.klox

data class LoxInstance(val klass: LoxClass) {
  val fields = mutableMapOf<String, Any?>()

  override fun toString(): String = "${klass.name} instance"

  fun get(name: Token): Any? {
    if (fields.containsKey(name.lexeme)) {
      return fields[name.lexeme]
    }
    throw RuntimeError(name, "Undefined property '${name.lexeme}'.")
  }

  fun set(name: Token, value: Any?) {
    fields[name.lexeme] = value
  }
}
