package com.donnations.backend.data.donations.queue

import com.donnations.backend.domain.model.DonationId
import com.donnations.backend.domain.queue.DonationQueue
import com.donnations.backend.domain.usecase.ProcessDonationUseCase
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

// In-process queue with one worker thread; pending items are lost on restart. Replace with a broker (SQS, RabbitMQ).
@Component
class InMemoryDonationQueue(
    private val processDonation: ProcessDonationUseCase,
) : DonationQueue, DisposableBean {
    private val log = LoggerFactory.getLogger(InMemoryDonationQueue::class.java)
    private val worker = Executors.newSingleThreadExecutor { Thread(it, "donation-worker").apply { isDaemon = true } }

    override fun enqueue(id: DonationId) {
        worker.execute {
            try {
                processDonation(id)
            } catch (e: Exception) {
                log.error("Failed to process donation ${id.value}; it is retried once its lease expires", e)
            }
        }
    }

    override fun destroy() {
        worker.shutdown()
        worker.awaitTermination(5, TimeUnit.SECONDS)
    }
}
