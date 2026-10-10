package com.donnations.core.base

// App-wide logging contract; use this instead of android.util.Log or println.
interface Logger {
    fun debug(tag: String, message: String)
    fun info(tag: String, message: String)
    fun warn(tag: String, message: String, throwable: Throwable? = null)
    fun error(tag: String, message: String, throwable: Throwable? = null)

    // Non-fatal or fatal exceptions worth reporting (crash reporter hooks in here).
    fun recordException(throwable: Throwable, message: String? = null)
}
