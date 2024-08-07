package com.ericrochester.klox

interface LoxCallable {
  fun arity(): Int
  fun call(interpreter: Interpreter, arguments: List<Any?>): Any?
}
