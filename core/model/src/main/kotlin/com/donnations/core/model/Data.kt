package com.donnations.core.model

// What a repository knows about a value: content is what's stored (may be stale),
// loading means an update is probably coming, error that the last load failed (content stays).
data class Data<out T>(
    val loading: Boolean = false,
    val content: T? = null,
    val error: Throwable? = null,
)
