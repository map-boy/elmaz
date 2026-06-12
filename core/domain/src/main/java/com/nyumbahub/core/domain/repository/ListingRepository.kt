package com.nyumbahub.core.domain.repository

import com.nyumbahub.core.domain.model.Inquiry
import com.nyumbahub.core.domain.model.Listing
import com.nyumbahub.core.domain.model.ListingType
import kotlinx.coroutines.flow.Flow

interface ListingRepository {
    fun getListings(): Flow<List<Listing>>
    fun getListings(type: ListingType?, city: String?): Flow<List<Listing>>
    suspend fun getListingById(id: String): Listing?
    suspend fun createListing(listing: Listing): Result<String>
    suspend fun updateListing(listing: Listing): Result<Unit>
    suspend fun deleteListing(id: String): Result<Unit>
    suspend fun saveListing(listingId: String): Result<Unit>
    suspend fun unsaveListing(listingId: String): Result<Unit>
    fun getSavedListings(): Flow<List<Listing>>
    suspend fun sendInquiry(inquiry: Inquiry, message: String): Result<Inquiry>
    fun getMyListings(userId: String): Flow<List<Listing>>
    suspend fun incrementViewCount(id: String)
}
