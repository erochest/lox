package com.ericrochester.klox

class Environment {
  val values = HashMap<String, Any?>()

  fun define(name: String, value: Any?) {
    values.put(name, value)
  }

  fun get(name: Token): Any? {
    if (values.containsKey(name.lexeme)) {
      return values.get(name.lexeme)
    }
    throw new RuntimeError("Undefined variable '{name.lexeme}'.")
  }
}
