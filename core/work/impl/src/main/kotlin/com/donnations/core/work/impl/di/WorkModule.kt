package com.donnations.core.work.impl.di

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.donnations.core.base.AppConfig
import com.donnations.core.base.Logger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object WorkModule {

    // Returned by the Application's Configuration.Provider,
    // so workers get Hilt injection.
    @Provides
    @Singleton
    fun provideWorkConfiguration(
        workerFactory: HiltWorkerFactory,
        appConfig: AppConfig,
        logger: Logger
    ): Configuration =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (appConfig.isStaging) Log.DEBUG else Log.ERROR)
            .setInitializationExceptionHandler {
                logger.recordException(
                    it,
                    "WorkManager failed to initialize"
                )
            }
            .setWorkerExecutionExceptionHandler {
                logger.recordException(
                    it.throwable,
                    "Worker ${it.workerClassName} crashed"
                )
            }
            .build()

    // Injected instead of WorkManager.getInstance(context),
    // so callers can be tested with a fake.
    @Provides
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}
