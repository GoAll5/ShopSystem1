package com.itmo.microservices.demo.orders.api

import java.util.*

data class AddProductToOrderRequest(
    val itemId: UUID,
    val amount: Int
)