package com.nyumbahub.feature.listings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nyumbahub.core.domain.model.Listing
import com.nyumbahub.core.domain.model.ListingType
import com.nyumbahub.core.domain.usecase.GetListingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListingsViewModel @Inject constructor(
    private val getListings: GetListingsUseCase
) : ViewModel() {

    private val _filterType = MutableStateFlow<ListingType?>(null)
    private val _city       = MutableStateFlow<String?>(null)

    val listings: StateFlow<List<Listing>> = combine(_filterType, _city) { type, city ->
        Pair(type, city)
    }.flatMapLatest { (type, city) ->
        getListings(type, city)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isLoading = MutableStateFlow(false)

    fun setFilter(type: ListingType?) { _filterType.value = type }
    fun setCity(city: String?)         { _city.value = city }
}
