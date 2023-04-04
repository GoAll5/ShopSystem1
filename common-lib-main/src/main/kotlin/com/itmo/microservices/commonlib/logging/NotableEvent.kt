package com.itmo.microservices.commonlib.logging

interface NotableEvent {
    fun getTemplate(): String
    fun getName(): String
}
