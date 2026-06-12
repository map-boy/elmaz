package com.nyumbahub.core.domain.model

data class Inquiry(
    val id: String = "",
    val listingId: String = "",
    val seekerId: String = "",
    val listerId: String = "",
    val status: InquiryStatus = InquiryStatus.OPEN,
    val createdAt: Long = 0L
)

enum class InquiryStatus {
    OPEN, REPLIED, CLOSED
}
