package com.nyumbahub.core.domain.model

data class Inquiry(
    val id: String = "",
    val listingId: String = "",
    val listingTitle: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val lastMessage: String = "",
    val status: InquiryStatus = InquiryStatus.OPEN,
    val unreadBySender: Long = 0L,
    val unreadByOwner: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

enum class InquiryStatus { OPEN, REPLIED, CLOSED }
