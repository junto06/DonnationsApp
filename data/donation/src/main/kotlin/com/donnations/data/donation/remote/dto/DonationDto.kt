package com.donnations.data.donation.remote.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class DonationRequestDto(
    @SerialName("donationId") val donationId: String, // client-generated idempotency key, reused on retries
    @SerialName("amount") val amount: String, // decimal string in major units ("25.00")
)

@Serializable(with = DonationStatusSerializer::class)
enum class DonationStatusDto { PENDING, PROCESSING, COMPLETED, FAILED, UNKNOWN }

object DonationStatusSerializer : KSerializer<DonationStatusDto> {
    override val descriptor = PrimitiveSerialDescriptor("DonationStatus", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): DonationStatusDto {
        val value = decoder.decodeString()
        return DonationStatusDto.entries.firstOrNull { it.name == value } ?: DonationStatusDto.UNKNOWN
    }

    override fun serialize(encoder: Encoder, value: DonationStatusDto) = encoder.encodeString(value.name)
}

@Serializable
data class DonationDto(
    @SerialName("id") val id: String,
    @SerialName("campaignId") val campaignId: String,
    @SerialName("currency") val currency: String,
    @SerialName("amount") val amount: String, // decimal string in major units ("25.00")
    @SerialName("status") val status: DonationStatusDto,
    @SerialName("failureCode") val failureCode: String? = null, // backend ErrorCode when FAILED
    @SerialName("createdAt") val createdAt: String,
)
