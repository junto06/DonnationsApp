package com.donnations.core.model

@JvmInline
value class CampaignId(val value: String) {
    init {
        require(value.isNotBlank()) { "CampaignId must not be blank" }
    }
}
