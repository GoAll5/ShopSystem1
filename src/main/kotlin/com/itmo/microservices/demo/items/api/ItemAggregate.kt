package com.itmo.microservices.demo.items.api

import ru.quipy.core.annotations.AggregateType
import ru.quipy.domain.Aggregate

@AggregateType(aggregateEventsTableName = "items")
class ItemAggregate : Aggregate