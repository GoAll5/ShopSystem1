package com.itmo.microservices.demo.users.api.service

import com.itmo.microservices.demo.users.api.model.AppUserModel
import com.itmo.microservices.demo.users.api.model.RegistrationRequest
import com.itmo.microservices.demo.users.impl.entity.AppUser
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

interface UserService {
    fun findUser(name: String): AppUserModel?
    fun registerUser(request: RegistrationRequest): AppUserModel
    fun getUserById(userId: UUID): AppUserModel
//    fun getAccountData(requester: UserDetails): AppUserModel
//    fun deleteUser(user: UserDetails)
}