package com.nyumbahub.core.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.nyumbahub.core.domain.model.Inquiry
import com.nyumbahub.core.domain.model.Listing
import com.nyumbahub.core.domain.model.ListingType
import com.nyumbahub.core.domain.repository.ListingRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class ListingRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ListingRepository {

    private val col = firestore.collection("listings")

    private fun safeListings(snap: com.google.firebase.firestore.QuerySnapshot): List<Listing> =
        snap.documents.mapNotNull { doc ->
            try { doc.toObject(Listing::class.java) } catch (e: Exception) { null }
        }

    override fun getListings(): Flow<List<Listing>> = callbackFlow {
        val sub = col.whereEqualTo("status", "ACTIVE").addSnapshotListener { snap, _ ->
            snap?.let { trySend(safeListings(it)) }
        }
        awaitClose { sub.remove() }
    }

    override fun getListings(type: ListingType?, city: String?): Flow<List<Listing>> = callbackFlow {
        var q = col.whereEqualTo("status", "ACTIVE")
        if (type != null) q = q.whereEqualTo("type", type.name)
        if (city != null) q = q.whereEqualTo("location.city", city)
        val sub = q.addSnapshotListener { snap, _ ->
            snap?.let { trySend(safeListings(it)) }
        }
        awaitClose { sub.remove() }
    }

    override suspend fun getListingById(id: String): Listing? =
        try { col.document(id).get().await().toObject(Listing::class.java) } catch (e: Exception) { null }

    override suspend fun createListing(listing: Listing): Result<String> = runCatching {
        val id = listing.id.ifBlank { UUID.randomUUID().toString() }
        col.document(id).set(listing.copy(id = id)).await()
        id
    }

    override suspend fun updateListing(listing: Listing): Result<Unit> = runCatching {
        col.document(listing.id).set(listing).await()
    }

    override suspend fun deleteListing(id: String): Result<Unit> = runCatching {
        col.document(id).delete().await()
    }

    override suspend fun saveListing(listingId: String): Result<Unit> = runCatching {
        firestore.collection("savedListings").document(listingId)
            .set(mapOf("listingId" to listingId, "savedAt" to System.currentTimeMillis())).await()
    }

    override suspend fun unsaveListing(listingId: String): Result<Unit> = runCatching {
        firestore.collection("savedListings").document(listingId).delete().await()
    }

    override fun getSavedListings(): Flow<List<Listing>> = callbackFlow {
        val sub = firestore.collection("savedListings").addSnapshotListener { snap, _ ->
            snap?.let { trySend(emptyList()) }
        }
        awaitClose { sub.remove() }
    }

    override suspend fun sendInquiry(inquiry: Inquiry, message: String): Result<Inquiry> = runCatching {
        val id = UUID.randomUUID().toString()
        val withId = inquiry.copy(id = id)
        firestore.collection("inquiries").document(id).set(withId).await()
        withId
    }

    override fun getMyListings(userId: String): Flow<List<Listing>> = callbackFlow {
        val sub = col.whereEqualTo("listerId", userId).addSnapshotListener { snap, _ ->
            snap?.let { trySend(safeListings(it)) }
        }
        awaitClose { sub.remove() }
    }

    override suspend fun incrementViewCount(id: String) {
        col.document(id).update("viewCount", com.google.firebase.firestore.FieldValue.increment(1)).await()
    }
}
