package com.donnations.core.network.impl

import com.donnations.core.base.AppConfig
import com.donnations.core.base.Environment
import javax.inject.Inject

internal class BaseUrl @Inject constructor(
    private val appConfig: AppConfig,
) {
    // Both point at the local backend (bootRun on :8080) until real hosts exist;
    // 10.0.2.2 is the emulator's alias for the host machine's localhost.
    operator fun invoke(): String = when (appConfig.environment) {
        Environment.PROD -> LOCAL_BACKEND
        Environment.STAGING -> LOCAL_BACKEND
    }

    private companion object {
        const val LOCAL_BACKEND = "http://10.0.2.2:8080/"
    }
}
