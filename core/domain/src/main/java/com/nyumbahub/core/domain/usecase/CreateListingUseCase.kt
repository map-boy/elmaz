package com.nyumbahub.core.domain.usecase

import com.nyumbahub.core.domain.model.Listing
import com.nyumbahub.core.domain.repository.ListingRepository
import javax.inject.Inject

class CreateListingUseCase @Inject constructor(
    private val repository: ListingRepository
) {
    suspend operator fun invoke(listing: Listing): Result<String> {
        if (listing.title.isBlank()) return Result.failure(Exception("Title is required"))
        if (listing.price <= 0)      return Result.failure(Exception("Price must be greater than 0"))
        return repository.createListing(listing)
    }
}
