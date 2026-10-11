package com.donnations.core.base

// Lets UI mappers build labels from string resources without depending on Android.
interface StringResolver {
    fun getString(id: Int, vararg formatArgs: Any): String
    fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any): String
}
