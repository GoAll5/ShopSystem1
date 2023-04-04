package com.itmo.microservices.demo.orders.controller


import com.itmo.microservices.demo.orders.projections.OrderView
import com.itmo.microservices.demo.orders.projections.OrderViewService
import com.itmo.microservices.demo.orders.projections.PaymentView
import com.itmo.microservices.demo.orders.projections.PaymentViewService
import com.itmo.microservices.demo.payment.api.MakePaymentResponse
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
class PaymentController(
        private val paymentViewService: PaymentViewService
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

    @GetMapping("/finlog")
    @Operation(
            summary = "get payments by order id",
            responses = [
                ApiResponse(description = "OK", responseCode = "200"),
                ApiResponse(description = "Unauthorized", responseCode = "403", content = [Content()]),
                ApiResponse(description = "Not found", responseCode = "404", content = [Content()])
            ],
            security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun getOrder(@RequestParam orderId: UUID?, @Parameter(hidden = true) @AuthenticationPrincipal requester: UserDetails) =
            paymentViewService.getPayments(orderId, requester.username)

    @PostMapping("/orders/{orderId}/payment")
    @Operation(
            summary = "make payment",
            responses = [
                ApiResponse(description = "OK", responseCode = "200"),
                ApiResponse(description = "Bad request", responseCode = "400", content = [Content()]),
                ApiResponse(description = "Unauthorized", responseCode = "403", content = [Content()])
            ],
            security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun makePayment(@PathVariable orderId: UUID, @Parameter(hidden = true) @AuthenticationPrincipal requester: UserDetails): MakePaymentResponse =
            paymentViewService.makePayment(orderId, requester.username)
}