package com.example.shop.auth.services.facades

import com.example.shop.admin.models.account.AdminAccountUpdateRequest
import com.example.shop.admin.models.auth.AuthorityCreateRequest
import com.example.shop.admin.models.auth.AuthorityUpdateRequest
import com.example.shop.auth.domain.AccountDomain
import com.example.shop.auth.domain.GroupMember
import com.example.shop.auth.repositories.GroupAuthorityRepository
import com.example.shop.auth.repositories.GroupMemberRepository
import com.example.shop.auth.services.AccountDomainService
import com.example.shop.auth.services.AccountService
import com.example.shop.auth.services.AuthorityService
import com.example.shop.common.apis.exceptions.BadRequestException
import com.example.shop.common.apis.models.AuthorityDto
import com.example.shop.common.logger.LogSupport
import com.example.shop.redis.authority_refresh.AuthorityRefreshEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccountAndAuthorityRelatedService(
    private val accountService: AccountService,
    private val accountDomainService: AccountDomainService,
    private val authorityService: AuthorityService,
    private val authorityRefreshEventPublisher: AuthorityRefreshEventPublisher,
    private val groupMemberRepository: GroupMemberRepository,
    private val groupAuthorityRepository: GroupAuthorityRepository,
) : LogSupport() {
    @Transactional
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

    @Transactional
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
    fun adminUpdateAccount(request: AdminAccountUpdateRequest): AccountDomain {
        val accountDomain = accountDomainService.findByAccountId(request.accountId)
            ?: throw BadRequestException("Account not found with the id of ${request.accountId}")

        // 유저의 활성상태(enabled), 권한(authority) 업데이트
        updateAccount(request, accountDomain)

        // 유저를 요청된 새로운 `AccountGroup`들에 가입 (`GroupMember` row들 생성)
        addUserToGroup(request, accountDomain)

        // 유저를 명시된 `AccountGroup`들로 부터 탈퇴 (`GroupMember` row들 삭제)
        removeUserFromGroup(request, accountDomain)

        return accountDomain
    }

    private fun updateAccount(request: AdminAccountUpdateRequest, accountDomain: AccountDomain) {
        // 유저의 enabled 상태 변경
        if (request.enabled != null) {
            accountDomain.account.enabled = request.enabled
        }

        // 유저의 권한 변경
        if (request.authorityName != null) {
            val newAuthority = authorityService.findByRoleName(request.authorityName) ?:
                throw BadRequestException("Authority not found with the name of ${request.authorityName}")
            accountDomain.account.authority = newAuthority
        }
        accountDomainService.saveAccount(accountDomain.account)
    }

    private fun addUserToGroup(request: AdminAccountUpdateRequest, accountDomain: AccountDomain) {
        if (request.addGroupIds != null) {
            // 가입할 그룹(`AccountGroup`)들과 각 그룹에 속한 권한(`GroupAuthority`)들
            val addingAccountGroupMap =
                groupAuthorityRepository.findAllByAccountGroupIdIn(request.addGroupIds).groupBy { it.accountGroup }

            addingAccountGroupMap.map { (accountGroup, groupAuthorities) ->
                // DB에 해당 유저를 해당 그룹에 추가 시킴
                groupMemberRepository.save(GroupMember(accountGroup, accountDomain.account))
                // AccountDomain에 내용 추가
                accountDomain.accountGroupMap[accountGroup] = groupAuthorities
            }
        }
    }

    private fun removeUserFromGroup(request: AdminAccountUpdateRequest, accountDomain: AccountDomain) {
        if (request.removeGroupIds != null) {
            groupMemberRepository.findAllByIdIn(request.removeGroupIds).map { groupMember ->
                // DB에서 해당 유저를 해당 그룹으로 부터 탈퇴 시킴
                groupMemberRepository.delete(groupMember)
                // AccountDomain 에서 내용 삭제
                accountDomain.accountGroupMap.remove(groupMember.accountGroup)
            }
        }
    }
}
