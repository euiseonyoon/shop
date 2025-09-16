package com.example.shop.refund.controllers

import com.example.shop.common.response.GlobalResponse
import com.example.shop.constants.ROLE_ADMIN
import com.example.shop.constants.ROLE_USER
import com.example.shop.refund.models.AdminUpdateRefundRequest
import com.example.shop.refund.models.RefundCancelRequest
import com.example.shop.refund.models.RefundRequest
import com.example.shop.refund.models.RefundRequestDto
import com.example.shop.refund.models.toRequestDto
import com.example.shop.refund.services.RefundService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/refund")
class RefundController(
    private val refundService: RefundService,
) {
    // authentication 여기
    @PreAuthorize("hasRole('$ROLE_USER')")
    @PostMapping
    fun requestRefund(
        @RequestBody request: RefundRequest,
        @AuthenticationPrincipal accountId: Long,
    ): GlobalResponse<RefundRequestDto> {
        return refundService.requestRefund(request, accountId).let {
            GlobalResponse.create(it.toRequestDto())
        }
    }

    // authentication 여기
    @PreAuthorize("hasRole('$ROLE_USER')")
    @PatchMapping
    fun cancelRefund(
        @RequestBody request: RefundCancelRequest,
        @AuthenticationPrincipal accountId: Long,
    ): GlobalResponse<RefundRequestDto> {
        return refundService.cancelRefund(request, accountId).let {
            GlobalResponse.create(it.toRequestDto())
        }
    }

    @PreAuthorize("hasRole('$ROLE_ADMIN')")
    @PatchMapping("/admin")
    fun updateRefund(
        @RequestBody request: AdminUpdateRefundRequest,
    ): GlobalResponse<RefundRequestDto> {
        return refundService.updateRefundStatusAsAdmin(request).let {
            GlobalResponse.create(it.toRequestDto())
        }
    }
}
