package com.nyumbahub.core.domain.usecase

import com.nyumbahub.core.domain.model.Inquiry
import com.nyumbahub.core.domain.repository.ListingRepository
import javax.inject.Inject

class SendInquiryUseCase @Inject constructor(
    private val listingRepository: ListingRepository
) {
    suspend operator fun invoke(
        listingId: String,
        seekerId: String,
        listerId: String,
        message: String
    ): Result<Inquiry> {
        val inquiry = Inquiry(
            listingId = listingId,
            seekerId  = seekerId,
            listerId  = listerId,
            createdAt = System.currentTimeMillis()
        )
        return listingRepository.sendInquiry(inquiry, message)
    }
}
