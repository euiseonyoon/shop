package com.example.shop.admin.controllers.auth

import com.example.shop.admin.controllers.models.AuthorityCreateRequest
import com.example.shop.admin.controllers.models.AuthorityUpdateRequest
import com.example.shop.common.apis.models.AuthorityDto
import com.example.shop.auth.services.AuthorityService
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
@RequestMapping(AdminAuthRoleController.URI)
class AdminAuthRoleController(
    private val authorityService: AuthorityService
) {
    companion object {
        const val URI = AdminAuthController.URI + "/authority"
    }

    // 1. Authority 조회
    // GET /admin/auth/authority?page=0&size=10&sort=hierarchy,asc
    @PreAuthorize("hasRole('${ROLE_SUPER_ADMIN}')")
    @GetMapping("")
    fun getRoles(pageable: Pageable): GlobalResponse<PagedResponse<AuthorityDto>> {
        val result = authorityService.findWithPage(pageable)
        return GlobalResponse.create(PagedResponse.fromPage(result))
    }

    @PreAuthorize("hasRole('${ROLE_SUPER_ADMIN}')")
    @PostMapping("")
    fun createRole(
        @RequestBody(required = true) @Valid request: AuthorityCreateRequest
    ): GlobalResponse<AuthorityDto> {
        return authorityService.createAuthority(request).let {
            GlobalResponse.create(it)
        }
    }

    @PreAuthorize("hasRole('${ROLE_SUPER_ADMIN}')")
    @PatchMapping("")
    fun updateRole(
        @RequestBody(required = true) @Valid request: AuthorityUpdateRequest
    ): GlobalResponse<AuthorityDto> {
        return authorityService.updateAuthorityHierarchy(request).let {
            GlobalResponse.create(it)
        }
    }
}
