package com.nyumbahub.core.domain.model

enum class SubscriptionPlan(
    val displayName: String,
    val monthlyPriceUsd: Double,
    val maxListings: Int,
    val maxPhotos: Int,
    val hasFeatured: Boolean,
    val hasVerifiedBadge: Boolean,
    val hasPriority: Boolean
) {
    FREE   ("Free",  0.0,  3,  10, false, false, false),
    BASIC  ("Basic", 5.0,  10, 20, false, false, false),
    PRO    ("Pro",   15.0, 30, 30, true,  true,  true),
    AGENT  ("Agent", 40.0, Int.MAX_VALUE, 30, true, true, true)
}
