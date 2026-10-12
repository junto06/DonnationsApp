package com.donnations.core.base

import org.jetbrains.annotations.VisibleForTesting
import java.io.IOException
import java.util.Collections
import java.util.WeakHashMap

// Records a failure once, unless it's
// connectivity/IOException or expected by the caller.
fun Logger.recordUnexpected(throwable: Throwable, message: String, isExpected: (Throwable) -> Boolean = { false }) {
    if (throwable is IOException || isExpected(throwable)) return
    if (!ReportedFailures.markFirstReport(throwable)) return
    recordException(throwable, message)
}

// Remembers reported failures (weakly) along their cause chain,
// so copies and wrappers of the same failure aren't reported twice.
internal object ReportedFailures {
    private val reported: MutableSet<Throwable> = Collections.newSetFromMap(WeakHashMap())

    fun markFirstReport(throwable: Throwable): Boolean = synchronized(reported) {
        val chain = generateSequence(throwable) { it.cause }.take(MAX_CAUSE_DEPTH).toList()
        if (chain.any { it in reported }) return false
        reported += chain
        true
    }

    @VisibleForTesting
    internal fun clear() =
        synchronized(reported) { reported.clear() }

    private const val MAX_CAUSE_DEPTH = 16
}
