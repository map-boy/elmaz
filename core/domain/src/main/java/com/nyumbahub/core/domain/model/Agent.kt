package com.nyumbahub.core.domain.model

data class Agent(
    val id: String = "",
    val userId: String = "",
    val agencyName: String = "",
    val licenseNo: String = "",
    val bio: String = "",
    val reviewScore: Float = 0f,
    val listingCount: Int = 0,
    val isVerified: Boolean = false,
    val avatarUrl: String = "",
    val phoneNumber: String = ""
)
