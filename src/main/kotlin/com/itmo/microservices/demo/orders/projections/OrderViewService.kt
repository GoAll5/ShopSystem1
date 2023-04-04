package com.itmo.microservices.demo.orders.projections

import com.fasterxml.jackson.annotation.JsonIgnore
import com.itmo.microservices.demo.common.exception.NotFoundException
import com.itmo.microservices.demo.items.api.BookItemsResponse
import com.itmo.microservices.demo.items.api.BookingView
import com.itmo.microservices.demo.items.projections.ItemViewService
import com.itmo.microservices.demo.orders.api.*
import com.itmo.microservices.demo.orders.logic.Order
import com.itmo.microservices.demo.orders.logic.OrderStatus
import com.itmo.microservices.demo.orders.logic.PaymentStatus
import com.itmo.microservices.demo.payment.api.FinancialOperationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import ru.quipy.core.EventSourcingService
import ru.quipy.domain.Unique
import ru.quipy.streams.AggregateSubscriptionsManager
import java.sql.Timestamp
import java.util.*
import javax.annotation.PostConstruct

@Service
class OrderViewService(
    private val ordersRepository: OrdersViewRepository,
    private val orderEsService: EventSourcingService<UUID, OrderAggregate, Order>,
    private val subscriptionsManager: AggregateSubscriptionsManager,
    private val itemViewService: ItemViewService)
{

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(OrderAggregate::class, "orders-view") {
            `when`(OrderCreatedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    ordersRepository.save(
                        OrderView(
                            event.orderId,
                            event.userId,
                            event.createdAt,
                            OrderStatus.COLLECTING,
                        )
                    )
                }
            }
            `when`(ProductAddedToOrder::class) { event ->
                withContext(Dispatchers.IO) {
                    val order = ordersRepository.findById(event.orderId).get()
                    order.itemsMap[event.itemId] = event.amount
                    ordersRepository.save(
                        OrderView(
                            event.orderId,
                            order.userId,
                            order.timeCreated,
                            status = OrderStatus.COLLECTING,
                            itemsMap = order.itemsMap,
                            deliveryDuration = order.deliveryDuration,
                            paymentHistory = order.paymentHistory,
                        )
                    )
                }
            }
            `when`(OrderBooked::class) { event ->
                withContext(Dispatchers.IO) {
                    val order = ordersRepository.findById(event.orderId).get()
                    ordersRepository.save(
                        OrderView(
                            event.orderId,
                            order.userId,
                            order.timeCreated,
                            status = OrderStatus.BOOKED,
                            itemsMap = event.itemsMap,
                            deliveryDuration = order.deliveryDuration,
                            paymentHistory = order.paymentHistory,
                            price = event.price
                        )
                    )
                }
            }
        }
    }

    fun createOrder(userId: String): OrderView {
        val event = orderEsService.create { it.createOrder(userId = userId) }
        return OrderView(
            event.orderId,
            event.userId,
            event.createdAt,
            OrderStatus.COLLECTING,
        )
    }

    fun addItem(orderId: UUID, itemId: UUID, amount: Int) {
        val order = ordersRepository.findByIdOrNull(orderId)
            ?: throw NotFoundException("Order with orderId=${orderId} not found")
        if (order.status != OrderStatus.COLLECTING) {
            throw IllegalArgumentException("Order with id=${orderId} is not in status COLLECTING, it's status is ${order.status}")
        }
        val itemFromItemService = itemViewService.getItemOrNull(itemId)
            ?: throw IllegalArgumentException("Item with id=${itemId} does not exists")
        orderEsService.update(orderId) { it.addProduct(itemId = itemId, amount) }
    }

    fun addSlotInSec(orderId: UUID, slotInSec: Int) {
        val order = ordersRepository.findByIdOrNull(orderId)
            ?: throw NotFoundException("Order with orderId=${orderId} not found")
        if (order.status != OrderStatus.BOOKED) {
            throw IllegalArgumentException("Order with id=${orderId} is not in status BOOKED, it's status is ${order.status}")
        }
        orderEsService.update(orderId) { it.addSlotInSec(slotInSec) }
    }

    fun getOrder(orderId: UUID): OrderView? {
        val order = ordersRepository.findByIdOrNull(orderId)
                ?: throw NotFoundException("Order with orderId=${orderId} not found")
        return OrderView(
            order.id,
            order.userId,
            order.timeCreated,
            order.status,
            order.itemsMap,
            order.deliveryDuration,
            order.paymentHistory,
        )
    }

    fun bookOrder(orderId: UUID): BookingView {
        val order = ordersRepository.findByIdOrNull(orderId)
            ?: throw NotFoundException("Order with orderId=${orderId} not found")
        if (order.status != OrderStatus.COLLECTING) {
            throw IllegalArgumentException("Order with id=${orderId} is not in status COLLECTING, it's status is ${order.status}")
        }
        val response = itemViewService.bookItems(orderId, order.itemsMap)
        orderEsService.update(orderId) {it.bookOrder(response.bookingView.failedItems, response.price)}

        return response.bookingView
    }
}

data class PaymentLogRecordView(
    val timestamp: Long,
    val status: PaymentStatus,
    val amount: Int,
    val transactionId: UUID,
)


@Document("orders-view")
data class OrderView(
    @Id
    override val id: UUID,
    @JsonIgnore
    val userId: String,
    val timeCreated: Long = 0,
    val status: OrderStatus,
    val itemsMap: MutableMap<UUID, Int> = mutableMapOf(),
    val deliveryDuration: Int? = null,
    val paymentHistory: List<PaymentLogRecordView> = emptyList(),
    @JsonIgnore
    val price: Int = 0,
) : Unique<UUID>


data class ItemInOrder(
    val title: String,
    val amount: Int,
    val price: Int,
)

@Repository
interface OrdersViewRepository : MongoRepository<OrderView, UUID> {
    fun findByUserId(userId: String): OrderView;
}
