package com.donnations.backend.domain.exception

sealed class DomainException(
    message: String,
    val errorCode: ErrorCode,
) : RuntimeException(message)

enum class ErrorCode(val value: String) {
    INTERNAL_ERROR("1001"),
    INVALID_REQUEST("1002"),
    ROUTE_NOT_FOUND("1003"),
    CAMPAIGN_NOT_FOUND("1004"),
    INVALID_DONATION_AMOUNT("1005"),
    CAMPAIGN_ENDED("1006"),
}

class CampaignNotFoundException :
    DomainException("Campaign not found", errorCode = ErrorCode.CAMPAIGN_NOT_FOUND)

class InvalidDonationAmountException :
    DomainException("Donation amount is invalid", errorCode = ErrorCode.INVALID_DONATION_AMOUNT)

class CampaignEndedException :
    DomainException("Campaign is no longer accepting donations", errorCode = ErrorCode.CAMPAIGN_ENDED)
