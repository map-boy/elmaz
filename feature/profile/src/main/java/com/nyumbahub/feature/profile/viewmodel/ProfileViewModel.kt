package com.nyumbahub.feature.profile.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nyumbahub.core.domain.model.Listing
import com.nyumbahub.core.domain.model.User
import com.nyumbahub.core.domain.repository.AuthRepository
import com.nyumbahub.core.domain.repository.ListingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val listingRepository: ListingRepository
) : ViewModel() {
    val currentUser: StateFlow<User?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    private val _myListings = MutableStateFlow<List<Listing>>(emptyList())
    val myListings: StateFlow<List<Listing>> = _myListings
    private val _savedListings = MutableStateFlow<List<Listing>>(emptyList())
    val savedListings: StateFlow<List<Listing>> = _savedListings
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    fun loadData(userId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            listingRepository.getMyListings(userId).collect {
                _myListings.value = it
                _isLoading.value = false
            }
        }
        viewModelScope.launch {
            listingRepository.getSavedListings().collect { _savedListings.value = it }
        }
    }
    fun signOut() { viewModelScope.launch { authRepository.signOut() } }
}
