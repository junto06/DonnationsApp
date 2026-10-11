package com.donnations.backend.domain.queue

import com.donnations.backend.domain.model.DonationId

interface DonationQueue {
    fun enqueue(id: DonationId)
}
