package com.itmo.microservices.demo.notifications.impl.repository

import com.itmo.microservices.demo.notifications.impl.entity.NotificationUser
import org.springframework.data.mongodb.repository.MongoRepository

interface NotificationUserRepository: MongoRepository<NotificationUser, String>