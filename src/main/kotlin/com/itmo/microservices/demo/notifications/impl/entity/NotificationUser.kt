package com.itmo.microservices.demo.notifications.impl.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.UUID

@Document
class NotificationUser {
    @Id
    var id: UUID? = null
    var name: String? = null
    // Ignoring surname because we don't need it for notifications

    constructor()

    constructor(id: UUID, name: String) {
        this.id = id
        this.name = name
    }
}