package com.itmo.microservices.demo.users.impl.repository

import org.springframework.data.mongodb.repository.MongoRepository
import com.itmo.microservices.demo.users.impl.entity.AppUser
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : MongoRepository<AppUser, UUID> {
    fun findByName(name: String): AppUser?
}