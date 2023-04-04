package com.itmo.microservices.demo.items.config

import com.itmo.microservices.demo.items.api.BookItemEvent
import com.itmo.microservices.demo.items.api.ItemAggregate
import com.itmo.microservices.demo.items.api.ItemCreatedEvent
import com.itmo.microservices.demo.items.logic.Item
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.quipy.core.AggregateRegistry

import ru.quipy.core.EventSourcingServiceFactory
import java.util.*
import javax.annotation.PostConstruct

@Configuration
class ItemConfig {

    @Autowired
    private lateinit var eventSourcingServiceFactory: EventSourcingServiceFactory


    @Autowired
    private lateinit var aggregateRegistry: AggregateRegistry

    @PostConstruct
    fun init() {
        aggregateRegistry.register(ItemAggregate::class, Item::class) {
            registerStateTransition(ItemCreatedEvent::class, Item::createNewItem)
        }
        aggregateRegistry.register(ItemAggregate::class, Item::class) {
            registerStateTransition(BookItemEvent::class, Item::bookItem)
        }
    }

    @Bean
    fun itemEsService() = eventSourcingServiceFactory.create<UUID, ItemAggregate, Item>()

}