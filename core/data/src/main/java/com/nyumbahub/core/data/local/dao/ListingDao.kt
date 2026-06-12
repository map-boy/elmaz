package com.nyumbahub.core.data.local.dao

import androidx.room.*
import com.nyumbahub.core.data.local.entity.ListingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingDao {
    @Query("SELECT * FROM listings ORDER BY createdAt DESC")
    fun getAllListings(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE type = :type ORDER BY createdAt DESC")
    fun getListingsByType(type: String): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE city = :city ORDER BY createdAt DESC")
    fun getListingsByCity(city: String): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE listerId = :userId ORDER BY createdAt DESC")
    fun getMyListings(userId: String): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE id = :id LIMIT 1")
    suspend fun getListingById(id: String): ListingEntity?

    @Upsert
    suspend fun upsertListings(listings: List<ListingEntity>)

    @Query("DELETE FROM listings WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM listings WHERE cachedAt < :threshold")
    suspend fun deleteStaleCache(threshold: Long)
}
