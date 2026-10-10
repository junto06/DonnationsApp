package com.donnations.core.network.impl

import com.donnations.core.base.AppConfig
import com.donnations.core.base.Environment
import javax.inject.Inject

internal class BaseUrl @Inject constructor(
    private val appConfig: AppConfig,
) {
    operator fun invoke(): String = when (appConfig.environment) {
        Environment.PROD -> "https://api.donnations.com/"
        Environment.STAGING -> "https://staging.api.donnations.com/"
    }
}
