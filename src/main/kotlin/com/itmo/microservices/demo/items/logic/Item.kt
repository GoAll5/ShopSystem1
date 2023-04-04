package com.itmo.microservices.demo.items.logic

import com.itmo.microservices.demo.items.api.BookItemEvent
import com.itmo.microservices.demo.items.api.ItemAggregate
import com.itmo.microservices.demo.items.api.ItemCreatedEvent
import com.itmo.microservices.demo.items.api.ItemCreateRequest
import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import java.util.*
import kotlin.math.max

class Item : AggregateState<UUID, ItemAggregate> {
    private lateinit var itemId: UUID
    private lateinit var title: String
    private lateinit var description: String
    private var price: Int = 100
    private var amount = 0

    override fun getId() = itemId

    fun createNewItem(
        itemId: UUID = UUID.randomUUID(),
        item: ItemCreateRequest
    ): ItemCreatedEvent {
        if (item.price <= 0) {
            throw IllegalArgumentException("Item $itemId should have price above 0, but given ${item.price}")
        }
        if (item.amount < 0) {
            throw IllegalArgumentException("Item $itemId should have amount above 0, but given ${item.amount}")
        }
        return ItemCreatedEvent(itemId, item.title, item.description, item.price, item.amount)
    }

    @StateTransitionFunc
    fun createNewItem(event: ItemCreatedEvent) {
        itemId = event.itemId
        title = event.title
        description = event.description
        price = event.price
        amount = event.amount
    }

    fun bookItem(
        itemId: UUID,
        amountToBook: Int,
        bookingId: UUID,
    ): BookItemEvent {
        if (amountToBook > amount) {
            return BookItemEvent(itemId, amount, amountToBook, BookingStatus.FAILED, bookingId, price)
        }
        return BookItemEvent(itemId, amount - amountToBook, amountToBook, BookingStatus.SUCCESS, bookingId, price)
    }

    @StateTransitionFunc
    fun bookItem(event: BookItemEvent) {
        amount = event.newAmount
    }
}