package com.nyumbahub.feature.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nyumbahub.core.domain.model.Listing
import com.nyumbahub.core.domain.usecase.SearchListingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchListings: SearchListingsUseCase
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val isLoading = MutableStateFlow(false)

    val results: StateFlow<List<Listing>> = _query
        .debounce(300)
        .flatMapLatest { q -> searchListings(query = q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun search(query: String) { _query.value = query }
}
