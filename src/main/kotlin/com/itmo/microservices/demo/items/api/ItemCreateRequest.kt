package com.itmo.microservices.demo.items.api

data class ItemCreateRequest(
    val title: String,
    val description: String = "",
    val price: Int,
    val amount: Int
)