package com.donnations.core.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

// Implemented by view models that inject the app-wide handler.
interface HasCoroutineExceptionHandler {
    val exceptionHandler: CoroutineExceptionHandler
}

// viewModelScope with the reporting handler installed (view models only).
// It reuses viewModelScope's Job, so it is cancelled with the view model like viewModelScope is.
val <T> T.viewModelReportingScope: CoroutineScope where T : ViewModel, T : HasCoroutineExceptionHandler
    get() = viewModelScope + exceptionHandler

fun <T> T.launchReporting(block: suspend CoroutineScope.() -> Unit): Job
        where T : ViewModel, T : HasCoroutineExceptionHandler =
    viewModelReportingScope.launch(block = block)

// supervisorScope that reports a failure escaping the block, then rethrows it.
// The handler is installed so a failed launch child is reported too,
// a failure thrown out of the block is reported by the catch below.
// Not needed for plain repository calls: logged {} already reports those.
suspend fun <T, R> T.supervisorScopeWithReporting(
    block: suspend CoroutineScope.() -> R
): R where T : ViewModel, T : HasCoroutineExceptionHandler = try {
    withContext(exceptionHandler) { supervisorScope(block) }
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    exceptionHandler.handleException(currentCoroutineContext(), e)
    throw e
}
