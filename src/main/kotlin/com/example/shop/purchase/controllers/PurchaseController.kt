package com.example.shop.purchase.controllers

import com.example.shop.common.response.GlobalResponse
import com.example.shop.common.response.PagedResponse
import com.example.shop.constants.ROLE_USER
import com.example.shop.purchase.domain.Purchase
import com.example.shop.purchase.models.PurchaseDirectlyRequest
import com.example.shop.purchase.services.PurchaseService
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/purchase")
@PreAuthorize("hasRole('$ROLE_USER')")
class PurchaseController(
    private val purchaseService: PurchaseService
) {
    @GetMapping
    fun getMyPurchases(
        @RequestParam(required = false) ids: List<Long>?,
        authentication: Authentication,
        pageable: Pageable,
    ): GlobalResponse<PagedResponse<Purchase>> {
        return purchaseService.getMyPurchases(ids, authentication, pageable).let {
            GlobalResponse.create(PagedResponse.fromPage(it))
        }
    }

    @PostMapping
    fun purchaseDirectly(
        @RequestBody @Valid request: PurchaseDirectlyRequest,
        authentication: Authentication,
    ): GlobalResponse<Purchase> {
        return purchaseService.purchaseDirectly(request, authentication).let {
            GlobalResponse.create(it)
        }
    }

    @PostMapping("/cart")
    fun purchaseCartItems(
        authentication: Authentication,
    ): GlobalResponse<Purchase?> {
        return purchaseService.purchaseByCart(authentication).let {
            GlobalResponse.create(it)
        }
    }
}
