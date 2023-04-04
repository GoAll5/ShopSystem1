package com.itmo.microservices.demo.orders.logic

import com.itmo.microservices.demo.orders.api.*
import com.itmo.microservices.demo.orders.projections.PaymentLogRecordView
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import ru.quipy.domain.Unique
import java.util.*


class Payment : AggregateState<UUID, PaymentAggregate> {

    private  lateinit var status: PaymentStatus
    private lateinit var transactionId: UUID
    private var amount: Int = 0

    override fun getId() = transactionId

    fun makePayment(transactionId: UUID = UUID.randomUUID(), userId:String, orderId: UUID, amount: Int): PaymentWithdrawnEvent {
        return PaymentWithdrawnEvent(transactionId, orderId, userId, amount )
    }

    @StateTransitionFunc
    fun makePayment(event: PaymentWithdrawnEvent) {
        transactionId = event.transactionId
        status = PaymentStatus.SUCCCESS
        amount = event.amount
    }
}



enum class PaymentStatus {
    FAILED,
    SUCCCESS
}


