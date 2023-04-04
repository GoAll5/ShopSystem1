package com.itmo.microservices.demo.tasks.impl.repository

import com.itmo.microservices.demo.tasks.impl.entity.Task
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TaskRepository : MongoRepository<Task, UUID> {
    fun deleteAllByAuthor(author: String)
}
