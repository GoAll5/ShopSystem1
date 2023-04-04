package com.itmo.microservices.demo.items.api

import com.itmo.microservices.demo.items.logic.BookingStatus
import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Event
import java.util.*

const val ITEM_CREATED = "ITEM_CREATED_EVENT"
const val ITEM_UPDATED = "ITEM_UPDATED_EVENT"
const val ITEM_DELETED = "ITEM_DELETED_EVENT"
const val BOOK_ITEM = "BOOK_ITEM"

@DomainEvent(name = ITEM_CREATED)
data class ItemCreatedEvent(
    val itemId: UUID,
    val title: String,
    val description: String,
    val price: Int,
    val amount: Int
) : Event<ItemAggregate>(
    name = ITEM_CREATED,
    createdAt = System.currentTimeMillis()
)

@DomainEvent(name = ITEM_UPDATED)
data class ItemUpdatedEvent(
    val itemId: UUID,
    val title: String,
    val description: String,
    val price: Int,
    val amount: Int
) : Event<ItemAggregate>(
    name = ITEM_UPDATED,
    createdAt = System.currentTimeMillis()
)

@DomainEvent(name = ITEM_DELETED)
data class ItemDeletedEvent(
    val itemId: UUID
) : Event<ItemAggregate>(
    name = ITEM_DELETED,
    createdAt = System.currentTimeMillis()
)

@DomainEvent(name = BOOK_ITEM)
data class BookItemEvent(
    val itemId: UUID,
    val newAmount: Int,
    val bookAmount: Int,
    val status: BookingStatus,
    val bookingId: UUID,
    val price: Int
) : Event<ItemAggregate>(
    name = BOOK_ITEM,
    createdAt = System.currentTimeMillis()
)