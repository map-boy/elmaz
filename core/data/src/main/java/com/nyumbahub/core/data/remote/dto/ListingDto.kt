package com.nyumbahub.core.data.remote.dto

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import com.nyumbahub.core.domain.model.*
import java.util.Date

data class ListingDto(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = "RENT",
    val propertyType: String = "HOUSE",
    val status: String = "ACTIVE",
    val price: Double = 0.0,
    val currency: String = "RWF",
    val negotiable: Boolean = false,
    val bedrooms: Int = 0,
    val bathrooms: Int = 0,
    val sizeSqm: Double = 0.0,
    val furnished: Boolean = false,
    val amenities: List<String> = emptyList(),
    val photos: List<String> = emptyList(),
    val listerId: String = "",
    val viewCount: Int = 0,
    val inquiryCount: Int = 0,
    val featuredUntil: Long = 0L,
    val country: String = "",
    val city: String = "",
    val district: String = "",
    val neighbourhood: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    @ServerTimestamp val createdAt: Date? = null
) {
    fun toDomain(): Listing = Listing(
        id           = id,
        title        = title,
        description  = description,
        type         = runCatching { ListingType.valueOf(type) }.getOrDefault(ListingType.RENT),
        propertyType = runCatching { PropertyType.valueOf(propertyType) }.getOrDefault(PropertyType.HOUSE),
        status       = runCatching { ListingStatus.valueOf(status) }.getOrDefault(ListingStatus.ACTIVE),
        price        = price,
        currency     = currency,
        negotiable   = negotiable,
        bedrooms     = bedrooms,
        bathrooms    = bathrooms,
        sizeSqm      = sizeSqm,
        furnished    = furnished,
        amenities    = amenities,
        photos       = photos,
        listerId     = listerId,
        viewCount    = viewCount,
        inquiryCount = inquiryCount,
        featuredUntil = featuredUntil,
        createdAt    = createdAt?.time ?: 0L,
        location     = Location(country, city, district, neighbourhood, latitude, longitude)
    )
}

fun Listing.toDto(): ListingDto = ListingDto(
    id           = id,
    title        = title,
    description  = description,
    type         = type.name,
    propertyType = propertyType.name,
    status       = status.name,
    price        = price,
    currency     = currency,
    negotiable   = negotiable,
    bedrooms     = bedrooms,
    bathrooms    = bathrooms,
    sizeSqm      = sizeSqm,
    furnished    = furnished,
    amenities    = amenities,
    photos       = photos,
    listerId     = listerId,
    viewCount    = viewCount,
    inquiryCount = inquiryCount,
    featuredUntil = featuredUntil ?: 0L,
    country      = location.country,
    city         = location.city,
    district     = location.district,
    neighbourhood = location.neighbourhood,
    latitude     = location.latitude,
    longitude    = location.longitude
)
