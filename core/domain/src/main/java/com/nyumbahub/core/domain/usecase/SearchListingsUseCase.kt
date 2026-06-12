package com.nyumbahub.core.domain.usecase

import com.nyumbahub.core.domain.model.Listing
import com.nyumbahub.core.domain.model.ListingType
import com.nyumbahub.core.domain.repository.ListingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SearchListingsUseCase @Inject constructor(
    private val listingRepository: ListingRepository
) {
    operator fun invoke(
        query: String = "",
        city: String = "",
        minPrice: Double = 0.0,
        maxPrice: Double = Double.MAX_VALUE,
        bedrooms: Int = 0,
        type: ListingType? = null,
        propertyType: String = ""
    ): Flow<List<Listing>> {
        return listingRepository.getListings().map { listings ->
            listings.filter { listing ->
                (query.isEmpty() || listing.title.contains(query, ignoreCase = true) ||
                        listing.location.neighbourhood.contains(query, ignoreCase = true) ||
                        listing.location.city.contains(query, ignoreCase = true)) &&
                (city.isEmpty() || listing.location.city.equals(city, ignoreCase = true)) &&
                (listing.price >= minPrice && listing.price <= maxPrice) &&
                (bedrooms == 0 || listing.bedrooms >= bedrooms) &&
                (type == null || listing.type == type) &&
                (propertyType.isEmpty() || listing.propertyType.name.equals(propertyType, ignoreCase = true))
            }
        }
    }
}
