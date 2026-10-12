package com.donnations.core.base

// Collects recorded exceptions for assertions.
class RecordingLogger : Logger {
    val recorded = mutableListOf<Throwable>()

    override fun debug(tag: String, message: String) = Unit
    override fun info(tag: String, message: String) = Unit
    override fun warn(tag: String, message: String, throwable: Throwable?) = Unit
    override fun error(tag: String, message: String, throwable: Throwable?) = Unit
    override fun recordException(throwable: Throwable, message: String?) {
        recorded += throwable
    }
}
