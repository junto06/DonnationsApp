package com.donnations.core.network.impl.di

import com.donnations.core.base.AppConfig
import com.donnations.core.base.Logger
import com.donnations.core.network.impl.BaseUrl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    // One client app-wide (Retrofit and Coil) so the connection pool, dispatcher and cache are shared.
    @Provides
    @Singleton
    fun provideOkHttpClient(appConfig: AppConfig, logger: Logger): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .apply {
            if (appConfig.isStaging) {
                val httpLogger = HttpLoggingInterceptor { logger.debug("OkHttp", it) }
                addNetworkInterceptor(httpLogger.setLevel(HttpLoggingInterceptor.Level.BASIC))
            }
        }
        .build()

    @Provides
    @Singleton
    fun provideConverterFactory(json: Json): Converter.Factory =
        json.asConverterFactory("application/json".toMediaType())

    @Provides
    @Singleton
    fun provideRetrofit(
        baseUrl: BaseUrl,
        client: OkHttpClient,
        converter: Converter.Factory,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl())
        .client(client)
        .addConverterFactory(converter)
        .build()
}
