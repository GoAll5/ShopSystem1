package com.itmo.microservices.demo.payment.api

import java.util.*

data class MakePaymentResponse(
        val transactionId: UUID,
        val timestamp: Long
)