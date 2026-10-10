package com.donnations.logging

import android.util.Log
import com.donnations.core.base.AppConfig
import com.donnations.core.base.Logger
import javax.inject.Inject
import javax.inject.Singleton

// debug/info only on staging; warnings, errors and recorded exceptions always.
@Singleton
class LogcatLogger @Inject constructor(
    private val appConfig: AppConfig,
) : Logger {

    override fun debug(tag: String, message: String) {
        if (appConfig.isStaging) Log.d(tag, message)
    }

    override fun info(tag: String, message: String) {
        if (appConfig.isStaging) Log.i(tag, message)
    }

    override fun warn(tag: String, message: String, throwable: Throwable?) {
        Log.w(tag, message, throwable)
    }

    override fun error(tag: String, message: String, throwable: Throwable?) {
        Log.e(tag, message, throwable)
    }

    // Attach a crash reporter (e.g. Crashlytics) here when one is added.
    override fun recordException(throwable: Throwable, message: String?) {
        Log.e(EXCEPTION_TAG, message ?: throwable.message ?: throwable::class.java.name, throwable)
    }

    private companion object {
        const val EXCEPTION_TAG = "RecordedException"
    }
}
