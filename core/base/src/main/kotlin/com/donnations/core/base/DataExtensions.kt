package com.donnations.core.base

import com.donnations.core.model.Data
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Maps the content, keeping loading and error. A failing block becomes an error instead of
// ending the stream, so collectors never need catch.
inline fun <T, R> Data<T>.mapContent(block: (T) -> R): Data<R> = try {
    Data(
        content = content?.let(block),
        error = error,
        loading = loading
    )
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Data(error = e, loading = loading)
}

fun <T, R> Flow<Data<T>>.mapContent(block: suspend (T) -> R): Flow<Data<R>> = map { data ->
    try {
        Data(
            content = data.content?.let { block(it) },
            error = data.error,
            loading = data.loading
        )
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Data(error = e, loading = data.loading)
    }
}

// The one rule for rendering: content wins (even stale or failed), then the error, then loading.
// Finished with nothing stored and no error (e.g. the fetch saved nothing) is an error, never an endless spinner.
inline fun <T, R> Data<T>.fold(
    onLoading: () -> R,
    onError: (Throwable) -> R,
    onContent: (T) -> R
): R {
    val value = content
    val failure = error
    return when {
        value != null -> onContent(value)
        failure != null -> onError(failure)
        loading -> onLoading()
        else -> onError(NoSuchElementException("Nothing stored"))
    }
}
