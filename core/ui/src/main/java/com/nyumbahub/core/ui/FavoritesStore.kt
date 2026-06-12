package com.nyumbahub.core.ui

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FavoritesStore {
    val savedIds = mutableStateListOf<String>()

    fun toggle(id: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (savedIds.contains(id)) {
            savedIds.remove(id)
            user?.uid?.let {
                FirebaseFirestore.getInstance()
                    .collection("savedListings").document(it)
                    .collection("items").document(id).delete()
            }
        } else {
            savedIds.add(id)
            user?.uid?.let {
                FirebaseFirestore.getInstance()
                    .collection("savedListings").document(it)
                    .collection("items").document(id)
                    .set(mapOf("listingId" to id, "savedAt" to System.currentTimeMillis()))
            }
        }
    }

    fun isSaved(id: String) = savedIds.contains(id)

    fun loadFromFirestore() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("savedListings").document(uid)
            .collection("items").get()
            .addOnSuccessListener { snap ->
                savedIds.clear()
                snap.documents.forEach { doc ->
                    doc.getString("listingId")?.let { savedIds.add(it) }
                }
            }
    }
}
