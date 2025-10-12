package com.example.shop.purchase.controllers

import com.example.shop.common.response.GlobalResponse
import com.example.shop.common.response.PagedResponse
import com.example.shop.constants.ROLE_USER
import com.example.shop.purchase.domain.Purchase
import com.example.shop.purchase.domain.PurchaseDomain
import com.example.shop.purchase.models.PurchaseDirectlyRequest
import com.example.shop.purchase.models.PurchaseResponse
import com.example.shop.purchase.services.PurchaseService
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
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

    @GetMapping("/approve")
    fun approvePurchase() {
        // 여기서 purchase를 uuid를 통해서 찾은다음
        // status를 확인
        // INVALID라면  purchase에 포함된 purchaseProduct의 구매수량만큼 재고를 원복시킨다.
        // 그렇지 않다면 토스 페이먼츠의 /payment/approve인가 하는 것을 호출하고 purchase 상태를 APPROVE로 저장 한다
    }

    @GetMapping("/fail")
    fun failPurchase() {
        // 여기서 purchase를 uuid를 통해서 찾은다음
        // status를 확인
        // INVALID라면  purchase에 포함된 purchaseProduct의 구매수량만큼 재고를 원복시킨다.
        // 그렇지 않다면 토스 페이먼츠의 purchase 상태를 FAIL로 저장 한다
    }
}
