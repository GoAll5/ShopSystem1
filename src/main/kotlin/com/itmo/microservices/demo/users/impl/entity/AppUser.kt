package com.itmo.microservices.demo.users.impl.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.util.UUID

@Document
class AppUser {

    @Id
    var id: UUID? = null
    @Indexed(unique = true)
    var name: String? = null
    var password: String? = null

    constructor()

    constructor(id: UUID?, name: String?, password: String?) {
        this.id = id
        this.name = name
        this.password = password
    }
}