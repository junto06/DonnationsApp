package com.donnations

import android.app.Application
import androidx.work.Configuration
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.donnations.core.base.Logger
import dagger.Lazy
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class DonnationsApp : Application(), SingletonImageLoader.Factory, Configuration.Provider {

    @Inject lateinit var okHttpClient: Lazy<OkHttpClient>
    @Inject lateinit var logger: Logger
    @Inject lateinit var workConfiguration: Lazy<Configuration>

    override fun onCreate() {
        super.onCreate()
        // Record crashes, then hand off to the platform handler so the process still dies normally.
        val platformHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            logger.recordException(throwable, "Uncaught exception on ${thread.name}")
            platformHandler?.uncaughtException(thread, throwable)
        }
    }

    // Images reuse the app's single OkHttpClient (shared pool and cache).
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory(callFactory = { okHttpClient.get() })) }
            .build()

    // WorkManager starts on first use with the Hilt worker factory (core:work:impl removes its startup initializer).
    override val workManagerConfiguration: Configuration
        get() = workConfiguration.get()
}
