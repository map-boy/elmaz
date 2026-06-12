package com.nyumbahub.core.domain.model

data class Listing(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val type: ListingType = ListingType.RENT,
    val propertyType: PropertyType = PropertyType.HOUSE,
    val status: ListingStatus = ListingStatus.ACTIVE,
    val price: Double = 0.0,
    val currency: String = "USD",
    val negotiable: Boolean = false,
    val bedrooms: Int = 0,
    val bathrooms: Int = 0,
    val sizeSqm: Double = 0.0,
    val furnished: Boolean = false,
    val location: Location = Location(),
    val amenities: List<String> = emptyList(),
    val photos: List<String> = emptyList(),
    val listerId: String = "",
    val viewCount: Int = 0,
    val inquiryCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val featuredUntil: Long? = null
)

enum class ListingType { SALE, RENT }
enum class PropertyType { HOUSE, APARTMENT, TOWNHOUSE, VILLA, COMMERCIAL, LAND, STUDIO }
enum class ListingStatus { DRAFT, ACTIVE, FEATURED, PAUSED, RENTED, SOLD, EXPIRED, REJECTED }

data class Location(
    val country: String = "",
    val city: String = "",
    val district: String = "",
    val neighbourhood: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

