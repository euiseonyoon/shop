package com.example.shop.auth.services

import com.example.shop.admin.controllers.models.AdminAccountUpdateRequest
import com.example.shop.auth.domain.Account
import com.example.shop.auth.domain.Authority
import com.example.shop.auth.domain.GroupMember
import com.example.shop.auth.repositories.AccountRepository
import com.example.shop.auth.repositories.GroupMemberRepository
import com.example.shop.auth.security.third_party.enums.ThirdPartyAuthenticationVendor
import com.example.shop.common.apis.exceptions.BadRequestException
import com.example.shop.common.apis.models.AccountSearchCriteria
import org.springframework.data.domain.Page
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authorityService: AuthorityService,
    private val accountGroupService: AccountGroupService,
    private val groupMemberRepository: GroupMemberRepository
) {
    @Transactional(readOnly = true)
    fun findWithAuthoritiesByEmail(email: String): Account? {
        return accountRepository.findWithAuthoritiesByEmail(email)
    }

    @Transactional(readOnly = true)
    fun findWithAuthoritiesById(accountId: Long): Account? {
        return accountRepository.findWithAuthoritiesById(accountId)
    }

    @Transactional
    fun createAccount(
        email: String,
        rawPassword: String,
        nickname: String?,
        thirdPartyOauthVendor: ThirdPartyAuthenticationVendor?,
        authority: Authority
    ): Account {
        val account = Account().apply {
            this.email = email
            this.password = passwordEncoder.encode(rawPassword)
            this.enabled = true
            this.nickname = nickname
            this.oauth = thirdPartyOauthVendor
            addRole(authority)
        }
        val savedAccount = accountRepository.save(account)
        return savedAccount
    }

    @Transactional(readOnly = true)
    fun searchWithCriteria(criteria: AccountSearchCriteria): Page<Account> {
        return accountRepository.findWithCriteria(criteria)
    }

    @Transactional
    fun adminUpdateAccount(request: AdminAccountUpdateRequest): Account {
        val account = accountRepository.findWithAuthoritiesById(request.accountId) ?:
            throw BadRequestException("Account not found with the id of ${request.accountId}")

        if (request.enabled != null) {
            account.enabled = request.enabled
        }

        if (request.authorityName != null) {
            val newAuthority = authorityService.findByRoleName(request.authorityName) ?:
                throw BadRequestException("Authority not found with the name of ${request.authorityName}")

            account.authority = newAuthority
        }

        if (request.addGroupNames != null) {
            accountGroupService.findAccountGroups(request.addGroupNames.toSet()).forEach {
                // GroupMember 생성자 안에서 Account-GroupMember 양방향 mapping 실행함
                groupMemberRepository.save(GroupMember(account, it))
            }
        }

        if (request.removeGroupNames != null) {
            val removingAccountGroup =
                accountGroupService.findAccountGroups(request.removeGroupNames.toSet()).mapNotNull { it.id }

            account.groupMemberMap.forEach { groupMember ->
                if (groupMember.accountGroup!!.id!! in removingAccountGroup) {
                    groupMemberRepository.delete(groupMember)
                    account.groupMemberMap.remove(groupMember)
                }
            }
        }

        return accountRepository.save(account)
    }
}
