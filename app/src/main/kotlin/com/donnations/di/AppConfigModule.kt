package com.donnations.di

import android.os.LocaleList
import com.donnations.BuildConfig
import com.donnations.core.base.AppConfig
import com.donnations.core.base.AppLanguage
import com.donnations.core.base.Environment
import com.donnations.core.base.LocaleHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.enums.enumEntries

@Module
@InstallIn(SingletonComponent::class)
object AppConfigModule {
    @Provides
    @Singleton
    fun provideAppConfig(): AppConfig = AppConfig(
        versionName = BuildConfig.VERSION_NAME,
        versionCode = BuildConfig.VERSION_CODE,
        environment = enumValueOrDefault(Environment.PROD) {
            it.name == BuildConfig.ENVIRONMENT
        },
        localeHandler = {
            val prefs = LocaleList.getDefault()
            AppLanguage.resolve(List(prefs.size()) { prefs[it] })
        },
    )

    @Provides
    fun provideLocaleHandler(appConfig: AppConfig): LocaleHandler = appConfig.localeHandler
}

private inline fun <reified T : Enum<T>> enumValueOrDefault(
    default: T,
    predicate: (T) -> Boolean
): T {
    return enumEntries<T>().firstOrNull(predicate) ?: default
}
