package com.ericrochester.klox

class RuntimeError(val token: Token, message: String): RuntimeException(message) {
}