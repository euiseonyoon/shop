package com.example.shop.admin.controllers.auth

import com.example.shop.admin.models.auth.GroupAuthorityCreateRequest
import com.example.shop.admin.models.auth.GroupAuthorityDeleteRequest
import com.example.shop.admin.models.auth.GroupAuthorityUpdateRequest
import com.example.shop.auth.services.GroupAuthorityService
import com.example.shop.common.apis.models.GroupAuthorityDto
import com.example.shop.common.response.GlobalResponse
import com.example.shop.common.response.PagedResponse
import com.example.shop.constants.ROLE_SUPER_ADMIN
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(AdminAuthGroupAuthorityController.URI)
@Validated
class AdminAuthGroupAuthorityController(
    private val groupAuthorityService: GroupAuthorityService
) {
    companion object {
        const val URI = AdminAuthController.URI + "/group-authority"
    }

    @PreAuthorize("hasRole('$ROLE_SUPER_ADMIN')")
    @GetMapping("")
    fun getGroupAuthorities(pageable: Pageable): GlobalResponse<PagedResponse<GroupAuthorityDto>> {
        val result = groupAuthorityService.findWithPage(pageable).map { it.toDto() }
        return GlobalResponse.create(PagedResponse.fromPage(result))
    }

    @PreAuthorize("hasRole('$ROLE_SUPER_ADMIN')")
    @PostMapping("")
    fun createGroupAuthority(
        @RequestBody(required = true) @Valid request: GroupAuthorityCreateRequest
    ): GlobalResponse<GroupAuthorityDto> {
        return groupAuthorityService.createGroupAuthority(request).let {
            GlobalResponse.create(it.toDto())
        }
    }

    @PreAuthorize("hasRole('$ROLE_SUPER_ADMIN')")
    @PatchMapping("")
    fun updateGroupAuthority(
        @RequestBody(required = true) @Validated request: GroupAuthorityUpdateRequest
    ): GlobalResponse<GroupAuthorityDto> {
        return groupAuthorityService.updateGroupAuthority(request).let {
            GlobalResponse.create(it.toDto())
        }
    }

    @PreAuthorize("hasRole('$ROLE_SUPER_ADMIN')")
    @DeleteMapping("")
    fun deleteGroupAuthorities(
        @RequestBody(required = true) @Valid request: GroupAuthorityDeleteRequest
    ): GlobalResponse<Unit> {
        return groupAuthorityService.deleteGroupAuthorities(request).let {
            GlobalResponse.create(it)
        }
    }
}
