package com.ericrochester.tools

import java.io.PrintWriter
import java.nio.file.Paths

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Usage: tools <output-dir>")
        System.exit(64)
    }

    val outputDir = args[0]
    defineAst(outputDir, "Expr", listOf(
        "Binary   : Expr left, Token operator, Expr right",
        "Grouping : Expr expression",
        "Literal  : Object value",
        "Unary    : Token operator, Expr right"
    ))
}

fun defineAst(outputDir: String, baseName: String, types: List<String>) {
    val path = Paths.get(outputDir, "$baseName.kt")
    val writer = PrintWriter(path.toFile(), "UTF-8")

    writer.println("package com.ericrochester.klox\n")
    writer.println("import com.ericrochester.klox.Token\n")
    writer.println("abstract class $baseName {")
    // writer.println("    abstract fun accept(visitor: ${baseName}Visitor<Any>): Any")
    writer.println("}\n")

    types.forEach { type ->
        val className = type.split(":")[0].trim()
        val fields = type.split(":")[1].trim()
        defineType(writer, baseName, className, fields)
    }

    writer.close()
}

fun defineType(writer: PrintWriter, baseName: String, className: String, fields: String) {
    writer.println("data class $className(")
    fields.split(",").forEach { field ->
        val type = field.trim().split(" ")[0].trim()
        val name = field.trim().split(" ")[1].trim()
        writer.println("    val $name: $type,")
    }
    writer.println(") : $baseName() {")
    writer.println("}\n")
}