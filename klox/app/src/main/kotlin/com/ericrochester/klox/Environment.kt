package com.ericrochester.klox

class Environment {
  val values = HashMap<String, Any?>()

  fun define(name: String, value: Any?) {
    values.put(name, value)
  }

  fun assign(name: Token, value: Any?) {
    if (values.containsKey(name.lexeme)) {
      values.put(name.lexeme, value)
      return
    }
    throw RuntimeError(name, "Undefined variable '{name.lexeme}'.")
  }

  fun get(name: Token): Any? {
    if (values.containsKey(name.lexeme)) {
      return values.get(name.lexeme)
    }
    throw RuntimeError(name, "Undefined variable '{name.lexeme}'.")
  }
}
