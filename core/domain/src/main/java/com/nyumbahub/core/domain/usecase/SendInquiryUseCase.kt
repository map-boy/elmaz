package com.nyumbahub.core.domain.usecase

import com.nyumbahub.core.domain.model.Inquiry
import com.nyumbahub.core.domain.model.Listing
import com.nyumbahub.core.domain.repository.ListingRepository
import javax.inject.Inject

class SendInquiryUseCase @Inject constructor(
    private val listingRepository: ListingRepository
) {
    suspend operator fun invoke(
        listing: Listing,
        senderId: String,
        senderName: String,
        ownerName: String,
        message: String
    ): Result<Inquiry> {
        val inquiry = Inquiry(
            listingId = listing.id,
            listingTitle = listing.title,
            senderId = senderId,
            senderName = senderName,
            ownerId = listing.listerId,
            ownerName = ownerName,
            lastMessage = message,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return listingRepository.sendInquiry(inquiry, message)
    }
}
