package com.donnations.core.base

class AppConfig(
    val versionName: String,
    val versionCode: Int,
    val environment: Environment,
    val localeHandler: LocaleHandler,
) {
    val isStaging: Boolean
        get() = environment == Environment.STAGING

    override fun toString(): String {
        return "AppConfig(versionName='$versionName', versionCode=$versionCode, environment=$environment, localeHandler=$localeHandler)"
    }
}

enum class Environment {
    PROD,
    STAGING,
}
