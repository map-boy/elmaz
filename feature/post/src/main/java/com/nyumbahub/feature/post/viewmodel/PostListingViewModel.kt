package com.nyumbahub.feature.post.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nyumbahub.core.domain.model.*
import com.nyumbahub.core.domain.usecase.CreateListingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostListingViewModel @Inject constructor(
    private val createListing: CreateListingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PostUiState>(PostUiState.Idle)
    val uiState: StateFlow<PostUiState> = _uiState

    fun submit(
        listerId: String,
        title: String,
        description: String,
        price: String,
        currency: String,
        bedrooms: String,
        bathrooms: String,
        sizeSqm: String,
        city: String,
        district: String,
        neighbourhood: String,
        listingType: ListingType = ListingType.RENT,
        propertyType: PropertyType = PropertyType.HOUSE,
        furnished: Boolean = false,
        amenities: List<String> = emptyList(),
        imageUrls: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _uiState.value = PostUiState.Loading
            val listing = Listing(
                title         = title.trim(),
                description   = description.trim(),
                price         = price.toDoubleOrNull() ?: 0.0,
                currency      = currency,
                bedrooms      = bedrooms.toIntOrNull() ?: 1,
                bathrooms     = bathrooms.toIntOrNull() ?: 1,
                sizeSqm       = sizeSqm.toDoubleOrNull() ?: 0.0,
                type          = listingType,
                propertyType  = propertyType,
                furnished     = furnished,
                amenities     = amenities,
                photos        = imageUrls,
                listerId      = listerId,
                status        = ListingStatus.ACTIVE,
                location      = Location(
                    city          = city.trim(),
                    district      = district.trim(),
                    neighbourhood = neighbourhood.trim()
                )
            )
            createListing(listing)
                .onSuccess { _uiState.value = PostUiState.Success }
                .onFailure { _uiState.value = PostUiState.Error(it.message ?: "Failed to post listing") }
        }
    }
}

sealed class PostUiState {
    object Idle    : PostUiState()
    object Loading : PostUiState()
    object Success : PostUiState()
    data class Error(val message: String) : PostUiState()
}
