package com.nyumbahub.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "listings")
data class ListingEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val type: String,
    val propertyType: String,
    val status: String,
    val price: Double,
    val currency: String,
    val bedrooms: Int,
    val bathrooms: Int,
    val sizeSqm: Double,
    val city: String,
    val district: String,
    val latitude: Double,
    val longitude: Double,
    val photosJson: String,
    val listerId: String,
    val createdAt: Long,
    val cachedAt: Long = System.currentTimeMillis()
)
