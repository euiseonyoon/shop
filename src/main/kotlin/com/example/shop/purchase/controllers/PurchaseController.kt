package com.example.shop.purchase.controllers

import com.example.shop.common.response.GlobalResponse
import com.example.shop.common.response.PagedResponse
import com.example.shop.constants.ROLE_USER
import com.example.shop.purchase.models.PurchaseApproveRequest
import com.example.shop.purchase.models.PurchaseApproveResult
import com.example.shop.purchase.models.PurchaseDirectlyRequest
import com.example.shop.purchase.models.PurchaseFailRequest
import com.example.shop.purchase.models.PurchaseResponse
import com.example.shop.purchase.services.PurchaseService
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
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
        @AuthenticationPrincipal accountId: Long,
        pageable: Pageable,
    ): GlobalResponse<PagedResponse<PurchaseResponse>> {
        return purchaseService.getMyPurchases(ids, accountId, pageable).let {
            val purchaseResponsePage = PurchaseResponse.fromPurchaseDomainPage(it)
            GlobalResponse.create(PagedResponse.fromPage(purchaseResponsePage))
        }
    }

    @PostMapping
    fun purchaseDirectly(
        @RequestBody @Valid request: PurchaseDirectlyRequest,
        @AuthenticationPrincipal accountId: Long,
    ): GlobalResponse<PurchaseResponse> {
        return purchaseService.purchaseDirectly(request, accountId).let {
            val response = PurchaseResponse.fromPurchaseDomain(it)
            GlobalResponse.create(response)
        }
    }

    @PostMapping("/cart")
    fun purchaseCartItems(
        @AuthenticationPrincipal accountId: Long,
    ): GlobalResponse<PurchaseResponse?> {
        return purchaseService.purchaseByCart(accountId).let {
            val response = it?.let { PurchaseResponse.fromPurchaseDomain(it) }
            GlobalResponse.create(response)
        }
    }

    @PostMapping("/approve")
    fun approvePurchase(
        @RequestBody @Valid request: PurchaseApproveRequest,
    ): GlobalResponse<PurchaseApproveResult> {
        return purchaseService.approvePurchase(request).let {
            GlobalResponse.create(it)
        }
    }

    @PostMapping("/fail")
    fun failPurchase(
        @RequestBody request: PurchaseFailRequest
    ): GlobalResponse<Nothing> {
        purchaseService.failPurchase(request)
        return GlobalResponse.create(false, null, null)
    }
}
