package com.donnations.core.base

fun interface StringResolver {
    fun getString(id: Int, vararg formatArgs: Any): String
}
