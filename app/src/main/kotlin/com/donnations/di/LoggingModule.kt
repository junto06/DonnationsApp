package com.donnations.di

import com.donnations.core.base.Logger
import com.donnations.logging.LogcatLogger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface LoggingModule {
    @Binds
    fun bindLogger(impl: LogcatLogger): Logger
}
