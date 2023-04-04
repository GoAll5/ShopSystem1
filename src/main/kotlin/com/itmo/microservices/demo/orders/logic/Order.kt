package com.itmo.microservices.demo.orders.logic

import com.itmo.microservices.demo.orders.api.*
import com.itmo.microservices.demo.orders.projections.PaymentLogRecordView
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import ru.quipy.domain.Unique
import java.util.*


class Order : AggregateState<UUID, OrderAggregate> {

    private lateinit var id: UUID
    private lateinit var userId: String
    private var startDate: Long = 0
    private var lastUpdateDate: Long = 0
    private lateinit var status: OrderStatus
    var itemsMap: MutableMap<UUID, Int> = mutableMapOf()
    private var deliveryDuration: Int? = null
    private var paymentHistory: List<PaymentLogRecordView> = emptyList()
    private var slotInSec: Int? = null

    override fun getId() = id

    fun createOrder(orderId: UUID = UUID.randomUUID(), userId: String): OrderCreatedEvent {
        return OrderCreatedEvent(orderId, userId)
    }

    fun addProduct(itemId: UUID, amount: Int): ProductAddedToOrder {
        if (amount <= 0) {
            throw IllegalArgumentException("Item ${itemId} should have amount above 0, but given ${amount}")
        }
        return ProductAddedToOrder(this.id, itemId, amount)
    }

    fun addSlotInSec(slotInSec: Int): SlotInSecAdded {
        if (slotInSec < 1) {
            throw IllegalArgumentException("SlotInSec should be above or equal to 1, but given ${slotInSec}")
        }
        return SlotInSecAdded(this.id, slotInSec)
    }

    fun bookOrder(failedItems: Set<UUID>, price: Int): OrderBooked {
        failedItems.forEach{ itemId ->
            itemsMap.remove(itemId)
        }
        status = OrderStatus.BOOKED
        return OrderBooked(this.id, itemsMap, price)
    }

    @StateTransitionFunc
    fun createOrder(event: OrderCreatedEvent) {
        id = event.orderId
        userId = event.userId
        status = OrderStatus.COLLECTING
        startDate = event.createdAt
        lastUpdateDate = event.createdAt
    }

    @StateTransitionFunc
    fun addProduct(event: ProductAddedToOrder) {
        itemsMap[event.itemId] = event.amount
        lastUpdateDate = event.createdAt
    }

    @StateTransitionFunc
    fun addSlotInSec(event: SlotInSecAdded) {
        slotInSec = event.slotInSec
    }

    @StateTransitionFunc
    fun bookOrder(event: OrderBooked) {
        itemsMap = event.itemsMap
    }

}




enum class OrderStatus {
    COLLECTING,
    MESSAGE_SEND,
    BOOKED,
    DISCARD,
    PAID,
    SHIPPING,
    REFUND,
    COMPLETED
}


