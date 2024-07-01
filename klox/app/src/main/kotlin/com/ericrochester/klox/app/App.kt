/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package com.ericrochester.klox.app

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.charset.StandardCharsets

import com.ericrochester.klox.Scanner
import com.ericrochester.klox.Token
import com.ericrochester.klox.TokenType
import com.ericrochester.klox.Parser
import com.ericrochester.klox.Interpreter
import com.ericrochester.klox.RuntimeError

val interpreter: Interpreter = Interpreter()

var hadError = false
var hadRuntimeError = false

fun main(args: Array<String>) {
    if (args.size > 1) {
        println("Usage: klox [script]")
        System.exit(64)
    } else if (args.size == 1) {
        runFile(args[0])
    } else {
        runPrompt()
    }
}

private fun runFile(path: String) {
    val bytes = Files.readAllBytes(Paths.get(path))
    val source = String(bytes, StandardCharsets.UTF_8)
    run(source)
    if (hadError) System.exit(65)
    if (hadRuntimeError) System.exit(70)
}

private fun runPrompt() {
    val reader = BufferedReader(InputStreamReader(System.`in`))

    while (true) {
        print("> ")
        val line = reader.readLine()
        if (line == null) break
        run(line)
        hadError = false
    }
}

private fun run(source: String) {
    val scanner = Scanner(source)
    val tokens = scanner.scanTokens()
    val parser = Parser(tokens)
    val expression = parser.parse()

    if (hadError) return

    expression?.let {
        interpreter.interpret(it)
    }
}

fun error(line: Int, message: String) {
    report(line, "", message)
}

fun error(token: Token, message: String) {
    if (token.type == TokenType.EOF) {
        report(token.line, " at end", message)
    } else {
        report(token.line, " at '${token.lexeme}'", message)
    }
}

fun report(line: Int, where: String, message: String) {
    System.err.println("[line $line] Error$where: $message")
    hadError = true
}

fun runtimeError(error: RuntimeError) {
    System.err.println("${error.message} [line ${error.token.line}]")
    hadRuntimeError = true
}
