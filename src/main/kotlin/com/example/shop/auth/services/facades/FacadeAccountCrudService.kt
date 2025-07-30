package com.example.shop.auth.services.facades

import com.example.shop.auth.ROLE_ADMIN
import com.example.shop.auth.ROLE_USER
import com.example.shop.auth.domain.Account
import com.example.shop.auth.security.third_party.enums.ThirdPartyAuthenticationVendor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FacadeAccountCrudService(
    private val accountAndGroupService: AccountAndGroupService
) {
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
            roleName = ROLE_USER,
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
            roleName = ROLE_ADMIN,
            groupNames = groupNames,
            assignGroupStrictly = true,
            createRoleIfNotExist = false,
        )
    }
}
