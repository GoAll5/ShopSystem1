package com.itmo.microservices.demo.orders.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.itmo.microservices.demo.orders.api.*
import com.itmo.microservices.demo.orders.logic.Order
import com.itmo.microservices.demo.orders.logic.Payment

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.quipy.core.*

import ru.quipy.database.EventStore
import ru.quipy.mapper.JsonEventMapper
import ru.quipy.streams.AggregateEventStreamManager
import ru.quipy.streams.AggregateSubscriptionsManager
import java.util.*
import javax.annotation.PostConstruct

@Configuration
class PaymentConfig {

    @Autowired
    private lateinit var eventSourcingServiceFactory: EventSourcingServiceFactory


    @Autowired
    private lateinit var aggregateRegistry: AggregateRegistry

    @PostConstruct
    fun init() {
        aggregateRegistry.register(PaymentAggregate::class, Payment::class) {
            registerStateTransition(PaymentWithdrawnEvent::class, Payment::makePayment)
        }
    }

    @Bean
    fun paymentEsService() = eventSourcingServiceFactory.create<UUID, PaymentAggregate, Payment>()
}