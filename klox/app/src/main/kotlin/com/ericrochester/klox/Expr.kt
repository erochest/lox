package com.ericrochester.klox

import com.ericrochester.klox.Token

abstract class Expr {
}

data class Binary(
    val left: Expr,
    val operator: Token,
    val right: Expr,
) : Expr() {
}

data class Grouping(
    val expression: Expr,
) : Expr() {
}

data class Literal(
    val value: Object,
) : Expr() {
}

data class Unary(
    val operator: Token,
    val right: Expr,
) : Expr() {
}

