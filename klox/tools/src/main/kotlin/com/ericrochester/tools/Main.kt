package com.ericrochester.tools

import java.io.PrintWriter
import java.nio.file.Paths

fun main(args: Array<String>) {
  if (args.size != 1) {
    println("Usage: tools <output-dir>")
    System.exit(64)
  }

  val outputDir = args[0]
  defineAst(
      outputDir,
      "Expr",
      listOf(
          "Comma    : List<Expr> expressions",
          "Assign   : Token name, Expr value",
          "Binary   : Expr left, Token operator, Expr right",
          "Call     : Expr callee, Token paren, List<Expr> arguments",
          "Get      : Expr obj, Token name",
          "Grouping : Expr expression",
          "Literal  : Any? value",
          "Logical  : Expr left, Token operator, Expr right",
          "Set      : Expr obj, Token name, Expr value",
          "Super    : Token keyword, Token method",
          "Ternary  : Expr condition, Expr thenBranch, Expr elseBranch",
          "This     : Token keyword",
          "Unary    : Token operator, Expr right",
          "Variable : Token name",
      )
  )
  defineAst(
      outputDir,
      "Stmt",
      listOf(
          "Block      : List<Stmt?> statements",
          "ClassStmt  : Token name, Variable? superclass, List<Function> methods",
          "Expression : Expr expression",
          "Function   : Token name, List<Token> params, List<Stmt> body",
          "If         : Expr condition, Stmt thenBranch, Stmt? elseBranch",
          "Print      : Expr expression",
          "ReturnStmt : Token keyword, Expr? value",
          "Var        : Token name, Expr? initializer",
          "While      : Expr condition, Stmt body"
      )
  )
}

fun defineAst(outputDir: String, baseName: String, types: List<String>) {
  val path = Paths.get(outputDir, "$baseName.kt")
  println("Generating $path")
  val writer = PrintWriter(path.toFile(), "UTF-8")

  writer.println("package com.ericrochester.klox\n")
  writer.println("import com.ericrochester.klox.Token\n")
  writer.println("abstract class $baseName {")
  writer.println("    abstract fun <R> accept(visitor: ${baseName}Visitor<R>): R")
  writer.println("}\n")
  defineVisitor(writer, baseName, types)

  types.forEach { type ->
    val (className, fields) = splitPair(":", type)
    defineType(writer, baseName, className, fields)
  }

  writer.close()
}

fun defineVisitor(writer: PrintWriter, baseName: String, types: List<String>) {
  writer.println("interface ${baseName}Visitor<R> {")
  types.forEach { type ->
    val (className, _) = splitPair(":", type)
    writer.println(
        "    fun visit$className${baseName}(${className.lowercase()}${baseName}: $className): R"
    )
  }
  writer.println("}\n")
}

fun defineType(writer: PrintWriter, baseName: String, className: String, fields: String) {
  writer.println("data class $className(")
  fields.split(",").forEach { field ->
    val (type, name) = splitPair(" ", field)
    writer.println("    val $name: $type,")
  }
  writer.println(") : $baseName() {")
  writer.println("    override fun <R> accept(visitor: ${baseName}Visitor<R>): R {")
  writer.println("        return visitor.visit$className${baseName}(this)")
  writer.println("    }")
  writer.println("}\n")
}

fun splitPair(on: String, field: String): Pair<String, String> {
  val parts = field.trim().split(on)
  return parts[0].trim() to parts[1].trim()
}
