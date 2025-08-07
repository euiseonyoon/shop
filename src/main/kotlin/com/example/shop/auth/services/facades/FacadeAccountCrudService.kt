package com.example.shop.auth.services.facades

import com.example.shop.constants.ROLE_ADMIN
import com.example.shop.constants.ROLE_USER
import com.example.shop.auth.domain.Account
import com.example.shop.auth.security.third_party.enums.ThirdPartyAuthenticationVendor
import com.example.shop.auth.utils.RoleHierarchyHelper
import com.example.shop.constants.ADMIN_HIERARCHY
import com.example.shop.constants.DEFAULT_USER_HIERARCHY
import org.springframework.stereotype.Service

@Service
class FacadeAccountCrudService(
    private val accountAndGroupService: AccountAndGroupService,
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
        groupNames: Set<String>,
    ): Account {
        return accountAndGroupService.createAccountAndAssignGroup(
            email = email,
            rawPassword = rawPassword,
            nickname = nickname,
            thirdPartyOauthVendor = thirdPartyOauthVendor,
            roleInfo = ROLE_USER to (getDefaultRoleHierarchy(ROLE_USER) ?: DEFAULT_USER_HIERARCHY),
            groupNames = groupNames,
            assignGroupStrictly = false,
            createRoleIfNotExist = true,
        )
    }

    // TODO: 여기에 hasRole("ROLE_ADMIN) && hasAuthority("어떤 authority") 적용하면 좋을 것 같다.
    fun createAdminAccount(
        email: String,
        rawPassword: String,
        nickname: String?,
        groupNames: Set<String>,
    ): Account {
        return accountAndGroupService.createAccountAndAssignGroup(
            email = email,
            rawPassword = rawPassword,
            nickname = nickname,
            thirdPartyOauthVendor = null,
            roleInfo = ROLE_ADMIN to (getDefaultRoleHierarchy(ROLE_ADMIN) ?: ADMIN_HIERARCHY),
            groupNames = groupNames,
            assignGroupStrictly = true,
            createRoleIfNotExist = false,
        )
    }
}
