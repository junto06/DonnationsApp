package com.donnations.di

import com.donnations.core.base.Logger
import com.donnations.core.base.ReportingCoroutineExceptionHandler
import com.donnations.logging.LogcatLogger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineExceptionHandler

@Module
@InstallIn(SingletonComponent::class)
interface LoggingModule {
    @Binds
    fun bindLogger(impl: LogcatLogger): Logger

    @Binds
    fun bindCoroutineExceptionHandler(impl: ReportingCoroutineExceptionHandler): CoroutineExceptionHandler
}
