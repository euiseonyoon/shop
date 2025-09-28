package com.example.shop.auth.services.facades

import com.example.shop.admin.models.account.AdminAccountUpdateRequest
import com.example.shop.admin.models.auth.AuthorityCreateRequest
import com.example.shop.admin.models.auth.AuthorityUpdateRequest
import com.example.shop.auth.domain.AccountDomain
import com.example.shop.auth.domain.RoleName
import com.example.shop.auth.models.AccountGroupRequest
import com.example.shop.auth.models.RoleRequest
import com.example.shop.auth.services.AccountDomainService
import com.example.shop.auth.services.AuthorityDomainService
import com.example.shop.common.apis.exceptions.BadRequestException
import com.example.shop.common.apis.models.AuthorityDto
import com.example.shop.common.logger.LogSupport
import com.example.shop.redis.authority_refresh.AuthorityRefreshEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccountAndAuthorityRelatedService(
    private val accountDomainService: AccountDomainService,
    private val authorityDomainService: AuthorityDomainService,
    private val authorityRefreshEventPublisher: AuthorityRefreshEventPublisher,
) : LogSupport() {
    @Transactional
    fun createAuthority(request: AuthorityCreateRequest): AuthorityDto {
        val createdAuthority = authorityDomainService.createAuthority(
            RoleRequest(RoleName(request.name), request.hierarchy, true)
        )
        publishAuthorityChangeEvent("AuthorityCreateInfo={name:${request.name}, hierarchy:${request.hierarchy}}")
        return createdAuthority.toDto()
    }

    @Transactional
    fun updateAuthorityHierarchy(request: AuthorityUpdateRequest): AuthorityDto {
        val updatedAuthority = authorityDomainService.updateAuthorityHierarchy(
            request.id,
            request.hierarchy,
        )
        publishAuthorityChangeEvent("AuthorityUpdateInfo={id:${request.id}, hierarchy:${request.hierarchy}}")
        return updatedAuthority.toDto()
    }

    private fun publishAuthorityChangeEvent(message: String) {
        try {
            authorityRefreshEventPublisher.publishAuthorityRefreshEvent(message)
        } catch (e: Throwable) {
            logger.error("Failed to create Authority(role) refresh event.")
        }
    }

    @Transactional
    fun adminUpdateAccount(request: AdminAccountUpdateRequest): AccountDomain {
        val accountDomain = accountDomainService.findByAccountId(request.accountId)
            ?: throw BadRequestException("Account not found with the id of ${request.accountId}")

        updateAccount(request, accountDomain)

        addAccountToAccountGroup(request, accountDomain)

        removeAccountFromAccountGroup(request, accountDomain)

        return accountDomain
    }

    private fun updateAccount(request: AdminAccountUpdateRequest, accountDomain: AccountDomain) {
        // 유저의 enabled 상태 변경
        if (request.enabled != null) {
            accountDomain.account.enabled = request.enabled
        }

        // 유저의 권한 변경
        if (request.authorityName != null) {
            val newAuthority = authorityDomainService.findByRoleName(RoleName(request.authorityName)) ?:
                throw BadRequestException("Authority not found with the name of ${request.authorityName}")
            accountDomain.account.authority = newAuthority
        }
        accountDomainService.saveAccount(accountDomain.account)
    }

    private fun addAccountToAccountGroup(request: AdminAccountUpdateRequest, accountDomain: AccountDomain) {
        if (request.addGroupIds != null) {
            val addingRequest = AccountGroupRequest(request.addGroupIds.toSet(), false)
            val addedResult = authorityDomainService.addAccountToAccountGroups(accountDomain.account, addingRequest)

            addedResult.map { (accountGroup, groupAuthorities) ->
                accountDomain.accountGroupMap[accountGroup] = groupAuthorities
            }
        }
    }

    private fun removeAccountFromAccountGroup(request: AdminAccountUpdateRequest, accountDomain: AccountDomain) {
        if (request.removeGroupIds != null) {
            val removedAccountGroupsFrom = authorityDomainService.removeAccountFromAccountGroup(
                accountDomain.account.id, request.removeGroupIds
            )

            removedAccountGroupsFrom.forEach {
                accountDomain.accountGroupMap.remove(it)
            }
        }
    }
}
