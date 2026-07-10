package com.nyumbahub.core.domain.model
data class User(
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val phone: String = "",
    val avatarUrl: String = "",
    val role: UserRole = UserRole.SEEKER,
    val agency: String = "",
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
enum class UserRole { GUEST, SEEKER, LISTER, AGENT }
