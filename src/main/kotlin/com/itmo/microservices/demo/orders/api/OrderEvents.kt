package com.itmo.microservices.demo.orders.api

import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Event
import java.util.*

const val ORDER_CREATED = "ORDER_CREATED_EVENT"
const val PRODUCT_ADDED_TO_ORDER = "PRODUCT_ADDED_TO_ORDER"
const val SLOT_IN_SEC_ADDED = "SLOT_IN_SEC_ADDED"
const val ORDER_BOOKED = "ORDER_BOOKED"

@DomainEvent(name = ORDER_CREATED)
data class OrderCreatedEvent(
    val orderId: UUID,
    var userId: String
) : Event<OrderAggregate>(
    name = ORDER_CREATED,
    createdAt = System.currentTimeMillis()
)

@DomainEvent(name = PRODUCT_ADDED_TO_ORDER)
data class ProductAddedToOrder(
    val orderId: UUID,
    val itemId: UUID,
    val amount: Int
) : Event<OrderAggregate>(
    name = PRODUCT_ADDED_TO_ORDER,
    createdAt = System.currentTimeMillis()
)

@DomainEvent(name = SLOT_IN_SEC_ADDED)
data class SlotInSecAdded(
    val orderId: UUID,
    val slotInSec: Int,
) : Event<OrderAggregate>(
    name = SLOT_IN_SEC_ADDED,
    createdAt = System.currentTimeMillis()
)

@DomainEvent(name = ORDER_BOOKED)
data class OrderBooked(
    val orderId: UUID,
    val itemsMap: MutableMap<UUID, Int>,
    val price: Int
) : Event<OrderAggregate>(
    name = ORDER_BOOKED,
    createdAt = System.currentTimeMillis()
)
