package com.nyumbahub.feature.listings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nyumbahub.core.domain.model.Listing
import com.nyumbahub.core.domain.usecase.SendInquiryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class ListerInfo(
    val displayName: String = "",
    val phone: String = "",
    val email: String = "",
    val agency: String = "",
    val isVerified: Boolean = false
)

data class ListingDetailUiState(
    val listing: Listing? = null,
    val listerInfo: ListerInfo? = null,
    val isLoading: Boolean = true,
    val isCreatingInquiry: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ListingDetailViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val sendInquiryUseCase: SendInquiryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListingDetailUiState())
    val uiState: StateFlow<ListingDetailUiState> = _uiState.asStateFlow()

    fun load(listingId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val doc = firestore.collection("listings").document(listingId).get().await()
                val listing = doc.toObject(Listing::class.java)
                _uiState.value = _uiState.value.copy(listing = listing, isLoading = false)

                val listerId = listing?.listerId ?: ""
                if (listerId.isNotEmpty()) {
                    val userDoc = firestore.collection("users").document(listerId).get().await()
                    _uiState.value = _uiState.value.copy(
                        listerInfo = ListerInfo(
                            displayName = userDoc.getString("displayName") ?: userDoc.getString("name") ?: "Owner",
                            phone       = userDoc.getString("phone") ?: "",
                            email       = userDoc.getString("email") ?: "",
                            agency      = userDoc.getString("agency") ?: "",
                            isVerified  = userDoc.getBoolean("isVerified") ?: false
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun sendInquiry(onSuccess: (String) -> Unit) {
        val state = _uiState.value
        val listing = state.listing ?: return
        val user = auth.currentUser ?: return
        if (state.isCreatingInquiry) return

        _uiState.value = state.copy(isCreatingInquiry = true)
        viewModelScope.launch {
            val senderName = user.displayName ?: user.email ?: "User"
            val ownerName = state.listerInfo?.displayName ?: "Owner"
            val result = sendInquiryUseCase(
                listing = listing,
                senderId = user.uid,
                senderName = senderName,
                ownerName = ownerName,
                message = "Hi, I'm interested in ${listing.title}. Is it still available?"
            )
            _uiState.value = _uiState.value.copy(isCreatingInquiry = false)
            result.onSuccess { onSuccess(it.id) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }
}
