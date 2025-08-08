package com.example.shop.auth.services.facades

import com.example.shop.admin.controllers.models.AuthorityCreateRequest
import com.example.shop.admin.controllers.models.AuthorityUpdateRequest
import com.example.shop.auth.domain.Account
import com.example.shop.auth.exceptions.AccountGroupPartiallyNotFoundException
import com.example.shop.auth.exceptions.AuthorityNotFoundException
import com.example.shop.auth.security.third_party.enums.ThirdPartyAuthenticationVendor
import com.example.shop.auth.services.AccountGroupService
import com.example.shop.auth.services.AccountService
import com.example.shop.auth.services.AuthorityService
import com.example.shop.auth.services.GroupMemberService
import com.example.shop.common.apis.models.AuthorityDto
import com.example.shop.common.utils.CustomAuthorityUtils
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccountAndAuthorityRelatedService(
    private val accountService: AccountService,
    private val accountGroupService: AccountGroupService,
    private val groupMemberService: GroupMemberService,
    private val authorityService: AuthorityService,
    private val customAuthorityUtils: CustomAuthorityUtils,
    private val redisTemplate: RedisTemplate<String, ByteArray>,
) {
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

    @Transactional
    fun createAuthority(request: AuthorityCreateRequest): AuthorityDto {
        val createdAuthorityDto = authorityService.createAuthority(request.name, request.hierarchy).toDto()

        val message = "AuthorityCreateInfo={name:${request.name}, hierarchy:${request.hierarchy}}"
        redisTemplate.convertAndSend("role-hierarchy-channel", message.toByteArray())

        return createdAuthorityDto
    }

    @Transactional
    fun updateAuthorityHierarchy(request: AuthorityUpdateRequest): AuthorityDto {
        val updatedAuthorityDto = authorityService.updateAuthorityHierarchy(request.id, request.hierarchy).toDto()

        val message = "AuthorityUpdateInfo={id:${request.id}, hierarchy:${request.hierarchy}}"
        redisTemplate.convertAndSend("role-hierarchy-channel", message.toByteArray())

        return updatedAuthorityDto
    }
}
