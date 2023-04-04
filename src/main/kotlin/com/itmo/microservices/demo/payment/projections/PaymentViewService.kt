package com.itmo.microservices.demo.orders.projections

import com.fasterxml.jackson.annotation.JsonIgnore
import com.itmo.microservices.demo.common.exception.NotFoundException
import com.itmo.microservices.demo.orders.api.*
import com.itmo.microservices.demo.orders.logic.OrderStatus
import com.itmo.microservices.demo.orders.logic.Payment
import com.itmo.microservices.demo.orders.logic.PaymentStatus
import com.itmo.microservices.demo.payment.api.FinancialOperationType
import com.itmo.microservices.demo.payment.api.MakePaymentResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import ru.quipy.core.EventSourcingService
import ru.quipy.domain.Unique
import ru.quipy.streams.AggregateSubscriptionsManager
import java.util.*
import javax.annotation.PostConstruct

@Service
class PaymentViewService(
        private val paymentRepository: PaymentViewRepository,
        private val paymentEsService: EventSourcingService<UUID, PaymentAggregate, Payment>,
        private val subscriptionsManager: AggregateSubscriptionsManager,
        private val orderViewService: OrderViewService)
{

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(PaymentAggregate::class, "payment-view") {
            `when`(PaymentWithdrawnEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    paymentRepository.save(
                            PaymentView(
                                    event.transactionId,
                                    PaymentStatus.SUCCCESS,
                                    event.userId,
                                    FinancialOperationType.WITHDRAW,
                                    event.amount,
                                    event.orderId,
                                    event.transactionId,
                                    event.createdAt,
                            )
                    )
                }
            }
        }
    }

    fun makePayment(orderId: UUID, userId: String): MakePaymentResponse {
        val order = orderViewService.getOrder(orderId)
                ?: throw NotFoundException("Order with orderId=${orderId} not found")

        if (order.status != OrderStatus.BOOKED) {
            throw IllegalArgumentException("Order with id=${orderId} is not in status BOOKED, it's status is ${order.status}")
        }
        val event = paymentEsService.create { it.makePayment(orderId=orderId, userId=userId, amount = order.price) }
        return MakePaymentResponse(
                event.transactionId,
                event.createdAt
        )
    }
    fun getPayments(orderId: UUID?, userId: String): List<PaymentView> {
        if (orderId != null) {
            return paymentRepository.findAllByUserIdAndOrderId(userId, orderId)
        }
        return paymentRepository.findAllByUserId(userId)
    }
}


@Document("payment-view")
data class PaymentView(
        @Id
        override val id: UUID,
        @JsonIgnore
        val status: PaymentStatus,
        @JsonIgnore
        val userId: String,
        val type: FinancialOperationType,
        val amount: Int,
        @JsonIgnore
        val orderId: UUID,
        val paymentTransactionId: UUID,
        val timestamp: Long
        ) : Unique<UUID>

@Repository
interface PaymentViewRepository : MongoRepository<PaymentView, UUID> {
    fun findAllByUserIdAndOrderId(userId: String, orderId: UUID): List<PaymentView>

    fun findAllByUserId(userId: String): List<PaymentView>
}
