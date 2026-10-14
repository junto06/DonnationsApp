package com.donnations.core.base

import com.donnations.core.model.Data
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

// Emits what's stored (loading while the network runs), then fetches and saves
// in the background so fromStorage emits again. Never throws:
// a failure comes out as Data.error with the last value kept.
fun <T, R> observeAndRefresh(
    fromStorage: Flow<T?>, // null while nothing is stored
    fromNetwork: suspend () -> R,
    toStorage: suspend (R) -> Unit,
): Flow<Data<T>> = flow {
    var last: T? = null // per collection, a new collection doesn't see the previous one's value
    fromStorage.combine(refreshing(fromNetwork, toStorage)) { cached, refresh ->
        Data(
            content = cached,
            error = refresh?.exceptionOrNull(),
            loading = refresh == null
        )
    }
        .onEach { last = it.content }
        .catch { emit(Data(content = last, error = it, loading = false)) }
        .collect { emit(it) }
}

// null (running) first, so combine can emit the stored value without waiting for the network.
private fun <R> refreshing(
    fromNetwork: suspend () -> R,
    toStorage: suspend (R) -> Unit
): Flow<Result<Unit>?> =
    flow {
        emit(null)
        emit(
            try {
                toStorage(fromNetwork())
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        )
    }
