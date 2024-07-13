package com.ericrochester.klox

data class LoxInstance(val klass: LoxClass) {

  override fun toString(): String = "${klass.name} instance"
}
