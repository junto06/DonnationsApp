package com.donnations.core.base

import kotlinx.coroutines.CoroutineExceptionHandler

// Rethrows, so an unhandled failure fails the test.
val testExceptionHandler = CoroutineExceptionHandler { _, e -> throw e }
