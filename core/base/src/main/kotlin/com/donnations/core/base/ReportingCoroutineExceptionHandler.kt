package com.donnations.core.base

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import javax.inject.Inject
import javax.inject.Singleton

// App-wide handler for uncaught coroutine failures
// reports them and keeps the app running
@Singleton
class ReportingCoroutineExceptionHandler @Inject constructor(
    private val logger: Logger,
) : AbstractCoroutineContextElement(CoroutineExceptionHandler), CoroutineExceptionHandler {
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        logger.recordUnexpected(exception, "Unhandled coroutine failure")
    }
}
