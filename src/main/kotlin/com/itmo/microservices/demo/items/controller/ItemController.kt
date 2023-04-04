package com.itmo.microservices.demo.items.controller


import com.itmo.microservices.demo.items.api.ItemCreateRequest
import com.itmo.microservices.demo.items.projections.BookingLogView
import com.itmo.microservices.demo.items.projections.ItemView
import com.itmo.microservices.demo.items.projections.ItemViewService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
class ItemController(
    private val itemViewService: ItemViewService
) {

    @ExceptionHandler(IllegalArgumentException::class)
    fun onIllegalArgumentException(e: IllegalArgumentException?) : ResponseEntity<String> {
        if (e != null) {
            return ResponseEntity.badRequest().body(e.message)
        }
        return ResponseEntity.badRequest().build()
    }

    @GetMapping("/items/")
    @Operation(
        summary = "Get all items",
        responses = [
            ApiResponse(description = "OK", responseCode = "200"),
            ApiResponse(description = "Unauthorized", responseCode = "403", content = [Content()])
        ],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun getItems(
        @RequestParam(value = "available", required = false, defaultValue = "false") available: Boolean,
        @RequestParam(value = "size", required = false, defaultValue = "100") size: Int,
    ): List<ItemView> =
        itemViewService.listItems(available, size)


    @GetMapping("/items/{itemId}")
    @Operation(
        summary = "Get item by id",
        responses = [
            ApiResponse(description = "OK", responseCode = "200"),
            ApiResponse(description = "Unauthorized", responseCode = "403", content = [Content()])
        ],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun getItem(@PathVariable itemId: UUID): ItemView? =
        itemViewService.getItem(itemId)


    @PostMapping("/_internal/catalogItem")
    @Operation(
        summary = "Create Item",
        responses = [
            ApiResponse(description = "OK", responseCode = "200"),
            ApiResponse(description = "Bad request", responseCode = "400", content = [Content()]),
            ApiResponse(description = "Unauthorized", responseCode = "403", content = [Content()])
        ],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun addItem(@RequestBody item: ItemCreateRequest): ItemView? = itemViewService.addItem(item)

    @PostMapping("/_internal/bookingHistory/{bookingId}")
    @Operation(
        summary = "Get list of booked items",
        responses = [
            ApiResponse(description = "OK", responseCode = "200"),
            ApiResponse(description = "Unauthorized", responseCode = "403", content = [Content()]),
            ApiResponse(description = "Not found", responseCode = "404", content = [Content()]),
        ],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun getBookingLogs(@PathVariable bookingId: UUID): List<BookingLogView> = itemViewService.getBookingLogs(bookingId)

}