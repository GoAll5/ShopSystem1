package com.itmo.microservices.demo.users.api.controller

import com.itmo.microservices.demo.tasks.api.model.TaskModel
import com.itmo.microservices.demo.users.api.model.AppUserModel
import com.itmo.microservices.demo.users.api.model.RegistrationRequest
import com.itmo.microservices.demo.users.api.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/users")
class UserController(private val userService: UserService) {

    @PostMapping
    @Operation(
        summary = "Register new user",
        responses = [
            ApiResponse(description = "OK", responseCode = "200"),
            ApiResponse(description = "Name is not unique", responseCode = "409", content = [Content()])
        ]
    )
    fun register(@RequestBody request: RegistrationRequest) = userService.registerUser(request)

    @GetMapping("/{userId}")
    @Operation(
        summary = "Get user by id",
        responses = [
            ApiResponse(description = "OK", responseCode = "200"),
            ApiResponse(description = "Unauthorized", responseCode = "403", content = [Content()]),
            ApiResponse(description = "User not found", responseCode = "404", content = [Content()])
        ],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun getUserById(@PathVariable userId: String): AppUserModel = userService.getUserById(UUID.fromString(userId))
//
//    @GetMapping("/me")
//    @Operation(
//        summary = "Get current user info",
//        responses = [
//            ApiResponse(description = "OK", responseCode = "200"),
//            ApiResponse(description = "User not found", responseCode = "404", content = [Content()])
//        ],
//        security = [SecurityRequirement(name = "bearerAuth")]
//    )
//    fun getAccountData(@Parameter(hidden = true) @AuthenticationPrincipal user: UserDetails): AppUserModel =
//            userService.getAccountData(user)
//
//    @DeleteMapping("/me")
//    @Operation(
//        summary = "Delete current user",
//        responses = [
//            ApiResponse(description = "OK", responseCode = "200"),
//            ApiResponse(description = "User not found", responseCode = "404", content = [Content()])
//        ],
//        security = [SecurityRequirement(name = "bearerAuth")]
//    )
//    fun deleteCurrentUser(@Parameter(hidden = true) @AuthenticationPrincipal user: UserDetails) =
//            userService.deleteUser(user)
}