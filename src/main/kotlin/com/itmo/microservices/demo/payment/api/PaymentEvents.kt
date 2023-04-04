package com.itmo.microservices.demo.orders.api

import com.itmo.microservices.demo.orders.logic.Payment
import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Event
import java.util.*

const val PAYMENT_WITHDRAWN = "PAYMENT_WITHDRAWN_EVENT"

@DomainEvent(name = PAYMENT_WITHDRAWN)
data class PaymentWithdrawnEvent(
        val transactionId: UUID,
        val orderId: UUID,
        val userId: String,
        val amount: Int,
) : Event<PaymentAggregate>(
        name = PAYMENT_WITHDRAWN,
        createdAt = System.currentTimeMillis()
)
