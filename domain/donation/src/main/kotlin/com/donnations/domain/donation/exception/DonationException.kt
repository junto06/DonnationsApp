package com.donnations.domain.donation.exception

sealed class DonationException(message: String) : Exception(message)

class CampaignEndedException : DonationException("Campaign is no longer accepting donations")

class CampaignNotFoundException : DonationException("Campaign not found")

class InvalidDonationAmountException : DonationException("Donation amount was rejected")
