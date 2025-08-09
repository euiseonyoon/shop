package com.example.shop.admin.controllers.auth

import com.example.shop.admin.models.auth.AccountGroupCreateRequest
import com.example.shop.admin.models.auth.AccountGroupUpdateRequest
import com.example.shop.auth.services.AccountGroupService
import com.example.shop.common.apis.models.AccountGroupDto
import com.example.shop.common.response.GlobalResponse
import com.example.shop.common.response.PagedResponse
import com.example.shop.constants.ROLE_SUPER_ADMIN
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(AdminAuthGroupController.URI)
class AdminAuthGroupController(
    private val groupService: AccountGroupService
) {
    companion object {
        const val URI = AdminAuthController.URI + "/account-group"
    }

    @PreAuthorize("hasRole('$ROLE_SUPER_ADMIN')")
    @GetMapping("")
    fun getGroups(pageable: Pageable): GlobalResponse<PagedResponse<AccountGroupDto>> {
        val result = groupService.findWithPage(pageable).map { it.toDto() }
        return GlobalResponse.create(PagedResponse.fromPage(result))
    }

    @PreAuthorize("hasRole('$ROLE_SUPER_ADMIN')")
    @PostMapping("")
    fun createGroup(
        @RequestBody(required = true) @Valid request: AccountGroupCreateRequest
    ): GlobalResponse<AccountGroupDto> {
        return groupService.createAccountGroup(request).let {
            GlobalResponse.create(it.toDto())
        }
    }

    @PreAuthorize("hasRole('$ROLE_SUPER_ADMIN')")
    @PatchMapping("")
    fun updateAccountGroup(
        @RequestBody(required = true) @Valid request: AccountGroupUpdateRequest
    ): GlobalResponse<AccountGroupDto> {
        return groupService.updateAccountGroup(request).let {
            GlobalResponse.create(it.toDto())
        }
    }
}
