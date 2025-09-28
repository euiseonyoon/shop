package com.example.shop.auth.services.facades

import com.example.shop.constants.ROLE_ADMIN
import com.example.shop.constants.ROLE_USER
import com.example.shop.auth.domain.AccountDomain
import com.example.shop.auth.domain.RoleName
import com.example.shop.auth.models.AccountGroupRequest
import com.example.shop.auth.models.AuthRequest
import com.example.shop.auth.models.RoleRequest
import com.example.shop.auth.security.third_party.enums.ThirdPartyAuthenticationVendor
import com.example.shop.auth.services.AccountDomainService
import com.example.shop.auth.utils.RoleHierarchyHelper
import com.example.shop.constants.ADMIN_HIERARCHY
import com.example.shop.constants.DEFAULT_USER_HIERARCHY
import com.example.shop.constants.SUPER_ADMIN_NAME
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class FacadeAccountCrudService(
    private val accountDomainService: AccountDomainService,
    private val roleHierarchyHelper: RoleHierarchyHelper,
) {
    private fun getDefaultRoleHierarchy(roleName: String): Int? {
        return roleHierarchyHelper.getRoleHierarchy(roleName)
    }

    fun createUserAccount(
        email: String,
        rawPassword: String,
        nickname: String?,
        thirdPartyOauthVendor: ThirdPartyAuthenticationVendor?,
        groupIds: Set<Long>,
    ): AccountDomain {
        val roleRequest = RoleRequest(
            RoleName(ROLE_USER),
            (getDefaultRoleHierarchy(ROLE_USER) ?: DEFAULT_USER_HIERARCHY),
            true
        )
        val groupRequest = AccountGroupRequest(groupIds, false)
        return accountDomainService.newAccountDomain(
            email = email,
            rawPassword = rawPassword,
            nickname = nickname,
            thirdPartyOauthVendor = thirdPartyOauthVendor,
            roleRequest = roleRequest,
            groupRequest = groupRequest,
        )
    }

    @PreAuthorize("hasRole('$SUPER_ADMIN_NAME')")
    fun createAdminAccount(
        email: String,
        rawPassword: String,
        nickname: String?,
        groupIds: Set<Long>,
    ): AccountDomain {

        val roleRequest = RoleRequest(
            RoleName(ROLE_ADMIN),
            (getDefaultRoleHierarchy(ROLE_ADMIN) ?: ADMIN_HIERARCHY),
            false
        )
        val groupRequest = AccountGroupRequest(groupIds, true)
        return accountDomainService.newAccountDomain(
            email = email,
            rawPassword = rawPassword,
            nickname = nickname,
            thirdPartyOauthVendor = null,
            roleRequest = roleRequest,
            groupRequest = groupRequest,
        )
    }
}
