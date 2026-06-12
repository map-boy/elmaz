package com.nyumbahub.core.domain.usecase

import com.nyumbahub.core.domain.model.Listing
import com.nyumbahub.core.domain.model.ListingType
import com.nyumbahub.core.domain.repository.ListingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetListingsUseCase @Inject constructor(
    private val repository: ListingRepository
) {
    operator fun invoke(type: ListingType? = null, city: String? = null): Flow<List<Listing>> =
        repository.getListings(type, city)
}
