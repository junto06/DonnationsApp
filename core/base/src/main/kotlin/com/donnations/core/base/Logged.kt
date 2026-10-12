package com.donnations.core.base

import kotlinx.coroutines.CancellationException

// Reports unexpected failures of the block, then rethrows them.
// Pass isExpected for normal domain rejections (e.g. campaign ended).
context(hasLogger: HasLogger)
suspend inline fun <T> logged(
    operation: String,
    noinline isExpected: (Throwable) -> Boolean = { false },
    block: suspend () -> T,
): T = try {
    hasLogger.logger.debug("Logged", "Starting $operation")
    block()
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    hasLogger.logger.recordUnexpected(e, "$operation failed", isExpected)
    throw e
}
