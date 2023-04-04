package com.itmo.microservices.demo.orders.controller


import com.itmo.microservices.demo.orders.api.AddProductToOrderRequest
import com.itmo.microservices.demo.orders.projections.OrderView
import com.itmo.microservices.demo.orders.projections.OrderViewService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import ru.quipy.core.EventSourcingService
import java.util.*


@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderViewService: OrderViewService
) {

    @ExceptionHandler(IllegalStateException::class)
    fun onIllegalStateException(e: IllegalStateException?): ResponseEntity<String> {
        val response = ResponseEntity.status(HttpStatus.FORBIDDEN)
        if (e != null) {
            response.body(e.message)
        }
        return response.build()
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun onIllegalArgumentException(e: IllegalArgumentException?): ResponseEntity<String> {
        if (e != null) {
            return ResponseEntity.badRequest().body(e.message)
        }
        return ResponseEntity.badRequest().build()
    }

    @GetMapping("/{orderId}")
    @Operation(
        summary = "Get order by id",
        responses = [
            ApiResponse(description = "OK", responseCode = "200"),
            ApiResponse(description = "Unauthorized", responseCode = "403", content = [Content()]),
            ApiResponse(description = "Not found", responseCode = "404", content = [Content()])
        ],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun getOrder(@PathVariable orderId: UUID) =
        orderViewService.getOrder(orderId)


    @PostMapping
    @Operation(
        summary = "Create order",
        responses = [
            ApiResponse(description = "OK", responseCode = "200"),
            ApiResponse(description = "Bad request", responseCode = "400", content = [Content()]),
            ApiResponse(description = "Unauthorized", responseCode = "403", content = [Content()])
        ],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun createOrder(@Parameter(hidden = true) @AuthenticationPrincipal requester: UserDetails): OrderView =
        orderViewService.createOrder(userId = requester.username)


    @PutMapping("/{orderId}/items/{itemId}")
    @Operation(
        summary = "Add Item to order",
        responses = [
            ApiResponse(description = "OK", responseCode = "200"),
            ApiResponse(description = "Bad request", responseCode = "400", content = [Content()]),
            ApiResponse(description = "Unauthorized", responseCode = "403", content = [Content()]),
            ApiResponse(description = "Not found", responseCode = "404", content = [Content()])
        ],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun addItem(
        @PathVariable orderId: UUID,
        @PathVariable itemId: UUID,
        @RequestParam amount: Int
    ) = orderViewService.addItem(orderId, itemId, amount)

    @PostMapping("/{orderId}/bookings")
    @Operation(
        summary = "Book order",
        responses = [
            ApiResponse(description = "OK", responseCode = "200"),
            ApiResponse(description = "Unauthorized", responseCode = "403", content = [Content()]),
            ApiResponse(description = "Not found", responseCode = "404", content = [Content()])
        ],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun bookOrder(
        @PathVariable orderId: UUID,
    ) = orderViewService.bookOrder(orderId)
}