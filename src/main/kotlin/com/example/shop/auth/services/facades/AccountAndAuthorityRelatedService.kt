package com.example.shop.auth.services.facades

import com.example.shop.admin.models.account.AdminAccountUpdateRequest
import com.example.shop.admin.models.auth.AuthorityCreateRequest
import com.example.shop.admin.models.auth.AuthorityUpdateRequest
import com.example.shop.auth.domain.Account
import com.example.shop.auth.domain.GroupMember
import com.example.shop.auth.exceptions.AccountGroupPartiallyNotFoundException
import com.example.shop.auth.exceptions.AuthorityNotFoundException
import com.example.shop.auth.repositories.GroupMemberRepository
import com.example.shop.auth.security.third_party.enums.ThirdPartyAuthenticationVendor
import com.example.shop.auth.services.AccountGroupService
import com.example.shop.auth.services.AccountService
import com.example.shop.auth.services.AuthorityService
import com.example.shop.auth.services.GroupMemberService
import com.example.shop.common.apis.exceptions.BadRequestException
import com.example.shop.common.apis.models.AuthorityDto
import com.example.shop.common.logger.LogSupport
import com.example.shop.common.utils.CustomAuthorityUtils
import com.example.shop.redis.authority_refresh.AuthorityRefreshEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccountAndAuthorityRelatedService(
    private val accountService: AccountService,
    private val accountGroupService: AccountGroupService,
    private val groupMemberService: GroupMemberService,
    private val authorityService: AuthorityService,
    private val customAuthorityUtils: CustomAuthorityUtils,
    private val authorityRefreshEventPublisher: AuthorityRefreshEventPublisher,
    private val groupMemberRepository: GroupMemberRepository,
) : LogSupport() {
    @Transactional
    fun createAccountAndAssignGroup(
        email: String,
        rawPassword: String,
        nickname: String?,
        thirdPartyOauthVendor: ThirdPartyAuthenticationVendor?,
        roleInfo: Pair<String, Int>,
        groupNames: Set<String>,
        assignGroupStrictly: Boolean,
        createRoleIfNotExist: Boolean,
    ): Account {
        val roleName = roleInfo.first
        customAuthorityUtils.validateAuthorityPrefix(roleName)

        val authority = authorityService.findByRoleName(roleName) ?: run {
            if (!createRoleIfNotExist) {
                throw AuthorityNotFoundException("$roleName authority not found.")
            }
            authorityService.createNewAuthority(roleInfo.first, roleInfo.second)
        }

        val savedAccount = accountService.createAccount(email, rawPassword, nickname, thirdPartyOauthVendor, authority)
        val foundGroups = accountGroupService.findAccountGroups(groupNames)

        if (assignGroupStrictly && groupNames.size != foundGroups.size) {
            throw AccountGroupPartiallyNotFoundException("Some account groups are not found. given group names: $groupNames")
        }
        val savedGroupMember = groupMemberService.setAccountGroup(savedAccount, foundGroups.toSet())

        return savedAccount
    }

    fun createAuthority(request: AuthorityCreateRequest): AuthorityDto {
        val createdAuthorityDto = authorityService.createAuthority(request.name, request.hierarchy).toDto()

        try {
            authorityRefreshEventPublisher.publishAuthorityRefreshEvent(
                "AuthorityCreateInfo={name:${request.name}, hierarchy:${request.hierarchy}}"
            )
        } catch (e: Throwable) {
            logger.error("Failed to create Authority(role) refresh event.")
        }

        return createdAuthorityDto
    }

    fun updateAuthorityHierarchy(request: AuthorityUpdateRequest): AuthorityDto {
        val updatedAuthorityDto = authorityService.updateAuthorityHierarchy(request.id, request.hierarchy).toDto()

        try {
            authorityRefreshEventPublisher.publishAuthorityRefreshEvent(
                "AuthorityUpdateInfo={id:${request.id}, hierarchy:${request.hierarchy}}"
            )
        } catch (e: Throwable) {
            logger.error("Failed to create Authority(role) refresh event.")
        }

        return updatedAuthorityDto
    }

    @Transactional
    fun adminUpdateAccount(request: AdminAccountUpdateRequest): Account {
        val account = accountService.findWithAuthoritiesById(request.accountId) ?:
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

        return accountService.save(account)
    }
}
