package com.itmo.microservices.demo.items.projections

import com.fasterxml.jackson.annotation.JsonIgnore
import com.itmo.microservices.demo.common.exception.NotFoundException
import com.itmo.microservices.demo.items.api.*
import com.itmo.microservices.demo.items.logic.BookingStatus
import com.itmo.microservices.demo.items.logic.Item
import com.itmo.microservices.demo.orders.logic.OrderStatus
import com.itmo.microservices.demo.orders.projections.OrderView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import ru.quipy.core.EventSourcingService
import ru.quipy.domain.Unique
import ru.quipy.streams.AggregateSubscriptionsManager
import java.sql.Timestamp
import java.util.*
import javax.annotation.PostConstruct

@Service
class ItemViewService(
    private val itemsRepository: ItemsViewRepository,
    private val bookingLogRepository: BookingLogRepository,
    private val subscriptionsManager: AggregateSubscriptionsManager,
    private val itemEsService: EventSourcingService<UUID, ItemAggregate, Item>,
) {

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(ItemAggregate::class, "items-view") {
            `when`(ItemCreatedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    itemsRepository.save(ItemView(event.itemId, event.title, event.description, event.price, event.amount))
                }
            }
            `when`(ItemUpdatedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    itemsRepository.save(ItemView(event.itemId, event.title, event.description, event.price, event.amount))
                }
            }
            `when`(ItemDeletedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    itemsRepository.deleteById(event.itemId)
                }
            }
            `when`(BookItemEvent::class) {event ->
                withContext(Dispatchers.IO) {
                    if (event.status == BookingStatus.SUCCESS) {
                        val item = itemsRepository.findById(event.itemId).get()
                        itemsRepository.save(
                            ItemView(
                                event.itemId,
                                item.title,
                                item.description,
                                item.price,
                                event.newAmount,
                            )
                        )
                    }
                    bookingLogRepository.save(
                        BookingLogView(
                            UUID.randomUUID(),
                            event.bookingId,
                            event.itemId,
                            event.status,
                            event.bookAmount,
                            event.createdAt
                        )
                    )
                }
            }
        }
    }

    fun getItemOrNull(itemId: UUID): ItemView?{
        return itemsRepository.findByIdOrNull(itemId)
    }

    fun getItem(itemId: UUID): ItemView {
        return getItemOrNull(itemId) ?: throw NotFoundException("Item with itemId=${itemId} not found")
    }

    fun listItems(available: Boolean = false, size: Int = 100): List<ItemView> {
        if (!available) {
            val ans = itemsRepository.findAll().take(size)
            return ans
        }
        return itemsRepository.findByAmountGreaterThan(0).take(size)
    }

    fun addItem(item: ItemCreateRequest): ItemView? {
        val event = itemEsService.create { it.createNewItem(item = item) }
        return ItemView(
            event.itemId,
            event.title,
            event.description,
            event.price,
            event.amount,
        )
    }

    fun bookItems(orderId: UUID, itemsMap: Map<UUID, Int>): BookItemsResponse {
        val failedItems: MutableSet<UUID> = mutableSetOf()
        val bookingId = UUID.randomUUID()
        var price = 0
        itemsMap.forEach{ (itemId, amount) ->
            val event = itemEsService.update(itemId) { it.bookItem(itemId, amount, bookingId) }
            if (event.status == BookingStatus.FAILED) {
                failedItems.add(itemId)
            } else {
                price += event.bookAmount * event.price
            }
        }
        return BookItemsResponse(BookingView(bookingId, failedItems), price)
    }

    fun getBookingLogs(bookingId: UUID): List<BookingLogView> {
        return bookingLogRepository.findByBookingId(bookingId)
    }
}


@Document("items-view")
data class ItemView(
    @Id
    override val id: UUID,
    val title: String,
    val description: String,
    val price: Int = 100,
    val amount: Int = 0
) : Unique<UUID>

@Document("booking-log-view")
data class BookingLogView(
    @Id
    @JsonIgnore
    override val id: UUID,
    val bookingId: UUID,
    val itemId: UUID,
    val status: BookingStatus,
    val amount: Int = 0,
    val timestamp: Long
) : Unique<UUID>


@Repository
interface ItemsViewRepository : MongoRepository<ItemView, UUID> {
    fun findByAmountGreaterThan(amount: Int) : List<ItemView>
}

@Repository
interface BookingLogRepository : MongoRepository<BookingLogView, UUID> {
    fun findByBookingId(bookingId: UUID): List<BookingLogView>
}
