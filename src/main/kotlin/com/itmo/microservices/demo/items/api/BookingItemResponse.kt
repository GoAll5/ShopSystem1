package com.itmo.microservices.demo.items.api

import java.util.*

data class BookingView(
        val id: UUID,
        val failedItems: MutableSet<UUID>
)

data class BookItemsResponse(
        val bookingView: BookingView,
        val price: Int
)