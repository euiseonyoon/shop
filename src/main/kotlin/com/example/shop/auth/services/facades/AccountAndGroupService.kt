package com.example.shop.auth.services.facades

import com.example.shop.auth.domain.Account
import com.example.shop.auth.exceptions.AccountGroupPartiallyNotFoundException
import com.example.shop.auth.exceptions.AuthorityNotFoundException
import com.example.shop.auth.security.third_party.enums.ThirdPartyAuthenticationVendor
import com.example.shop.auth.services.AccountGroupService
import com.example.shop.auth.services.AccountService
import com.example.shop.auth.services.AuthorityService
import com.example.shop.auth.services.GroupMemberService
import com.example.shop.common.utils.AuthorityUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class AccountAndGroupService(
    private val accountService: AccountService,
    private val accountGroupService: AccountGroupService,
    private val groupMemberService: GroupMemberService,
    private val authorityService: AuthorityService,
) {
    @Transactional
    fun createAccountAndAssignGroup(
        email: String,
        rawPassword: String,
        nickname: String?,
        thirdPartyOauthVendor: ThirdPartyAuthenticationVendor?,
        roleName: String,
        groupNames: Set<String>,
        assignGroupStrictly: Boolean,
    ): Account {
        AuthorityUtils.validateAuthorityPrefix(roleName)

        val authority = authorityService.findByRoleName(roleName)
            ?: throw AuthorityNotFoundException("$roleName authority not found.")

        val savedAccount = accountService.createAccount(email, rawPassword, nickname, thirdPartyOauthVendor, authority)
        val foundGroups = accountGroupService.findAccountGroups(groupNames)

        if (assignGroupStrictly && groupNames.size != foundGroups.size) {
            throw AccountGroupPartiallyNotFoundException("Some account groups are not found. given group names: $groupNames")
        }
        val savedGroupMember = groupMemberService.setAccountGroup(savedAccount, foundGroups.toSet())

        return savedAccount
    }
}
