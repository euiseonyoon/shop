package com.example.shop.admin.controllers.account

import com.example.shop.admin.controllers.models.AdminAccountUpdateRequest
import com.example.shop.auth.domain.extension_functions.toAdminAccountDto
import com.example.shop.common.apis.models.AccountSearchCriteria
import com.example.shop.auth.services.AccountService
import com.example.shop.common.apis.models.AdminAccountDto
import com.example.shop.common.response.GlobalResponse
import com.example.shop.common.response.PagedResponse
import com.example.shop.constants.ADMIN_ACCOUNT_URI_PREFIX
import com.example.shop.constants.ROLE_ADMIN
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(ADMIN_ACCOUNT_URI_PREFIX)
class AdminAccountController(
    private val accountService: AccountService
) {
    @PreAuthorize("hasRole('$ROLE_ADMIN')")
    @GetMapping("")
    fun getAccount(
        @RequestParam(required = false) accountId: List<Long>?,
        @RequestParam(required = false) emails: List<String>?,
        @RequestParam(required = false) enabled: Boolean?,
        pageable: Pageable
    ): GlobalResponse<PagedResponse<AdminAccountDto>> {
        val requestCriteria = AccountSearchCriteria(
            accountId, emails, enabled, pageable
        )
        val result = accountService.searchWithCriteria(requestCriteria).map {
            it.toAdminAccountDto()
        }
        return GlobalResponse.create(PagedResponse.fromPage(result))
    }

    @PreAuthorize("hasRole('$ROLE_ADMIN')")
    @PatchMapping("")
    fun updateAccount(
        @RequestBody(required = true) request: AdminAccountUpdateRequest
    ): GlobalResponse<AdminAccountDto> {
        return accountService.adminUpdateAccount(request).toAdminAccountDto().let {
            GlobalResponse.create(it)
        }
    }
}
