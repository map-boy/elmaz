package com.nyumbahub.core.domain.model

data class Message(
    val id: String = "",
    val inquiryId: String = "",
    val senderId: String = "",
    val text: String = "",
    val mediaUrl: String = "",
    val sentAt: Long = 0L,
    val readAt: Long = 0L,
    val isRead: Boolean = false
)
