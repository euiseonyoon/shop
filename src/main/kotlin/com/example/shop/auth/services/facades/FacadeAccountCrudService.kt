package com.example.shop.auth.services.facades

import com.example.shop.auth.ROLE_ADMIN
import com.example.shop.auth.ROLE_USER
import com.example.shop.auth.domain.Account
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FacadeAccountCrudService(
    private val accountAndGroupService: AccountAndGroupService
) {
    @Transactional
    fun createUserAccount(
        email: String,
        rawPassword: String,
        nickname: String?,
        groupNames: Set<String>,
    ): Account {
        return accountAndGroupService.createAccountAndAssignGroup(
            email = email,
            rawPassword = rawPassword,
            nickname = nickname,
            roleName = ROLE_USER,
            groupNames = groupNames,
            assignGroupStrictly = false,
        )
    }

    @Transactional
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
            roleName = ROLE_ADMIN,
            groupNames = groupNames,
            assignGroupStrictly = true,
        )
    }
}
