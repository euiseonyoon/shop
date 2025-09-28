package com.example.shop.auth.services

import com.example.shop.auth.domain.Account
import com.example.shop.auth.domain.AccountGroup
import com.example.shop.auth.domain.Authority
import com.example.shop.auth.domain.GroupAuthority
import com.example.shop.auth.domain.GroupMember
import com.example.shop.auth.domain.Role
import com.example.shop.auth.exceptions.AccountGroupPartiallyNotFoundException
import com.example.shop.auth.models.AccountGroupRequest
import com.example.shop.auth.models.RoleRequest
import com.example.shop.auth.repositories.GroupAuthorityRepository
import com.example.shop.auth.repositories.GroupMemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthorityDomainService(
    private val authorityService: AuthorityService,
    private val groupAuthorityRepository: GroupAuthorityRepository,
    private val groupMemberRepository: GroupMemberRepository,
) {
    @Transactional
    fun getOrCreateAuthority(roleRequest: RoleRequest): Authority {
        return authorityService.getOrCreateAuthority(roleRequest)
    }

    @Transactional
    fun createAuthority(roleRequest: RoleRequest): Authority {
        return authorityService.createNewAuthority(roleRequest)
    }

    @Transactional(readOnly = true)
    fun findByRole(role: Role): Authority? = authorityService.findByRole(role)

    @Transactional
    fun updateAuthorityHierarchy(authorityId: Long, hierarchy: Int): Authority {
        return authorityService.updateAuthorityHierarchy(authorityId, hierarchy)
    }

    @Transactional
    fun addAccountToAccountGroups(
        account: Account,
        groupRequest: AccountGroupRequest,
    ): Map<AccountGroup, List<GroupAuthority>> {
        val targetGroupIds = groupRequest.groupIds.toList()
        val groupAndGroupAuthorityMap =
            groupAuthorityRepository.findAllByAccountGroupIdIn(targetGroupIds).groupBy { it.accountGroup }

        validateAccountGroups(groupRequest.assignGroupStrictly, groupRequest.groupIds, groupAndGroupAuthorityMap.keys)

        groupAndGroupAuthorityMap.keys.map {
            groupMemberRepository.save(GroupMember(it, account))
        }
        return groupAndGroupAuthorityMap
    }

    private fun validateAccountGroups(
        assignGroupStrictly: Boolean,
        groupIds: Set<Long>,
        accountGroups: Set<AccountGroup>,
    ) {
        if (assignGroupStrictly && groupIds.size != accountGroups.size) {
            throw AccountGroupPartiallyNotFoundException(
                "Some account groups are not found. given group names: $groupIds"
            )
        }
    }

    @Transactional(readOnly = true)
    fun getGroupAuthoritiesByAccountId(accountId: Long): List<GroupAuthority> {
        return groupAuthorityRepository.getAccountGroupAuthorities(accountId)
    }

    @Transactional(readOnly = true)
    fun getGroupAuthoritiesByAccountIds(accountIds: List<Long>): Map<Account, List<GroupAuthority>> {
        return groupAuthorityRepository.getAccountGroupAuthorityDtos(accountIds)
            .groupBy { it.account }.mapValues { (_, value) ->
                value.map { it.groupAuthority }
            }
    }

    @Transactional
    fun removeAccountFromAccountGroup(accountId: Long, accountGroupIds: List<Long>): List<AccountGroup> {
        val removedFromAccountGroups = groupMemberRepository.findAllByAccountGroupAndAccountId(
            accountGroupIds,
            listOf(accountId),
        ).map { groupMember ->
            groupMemberRepository.delete(groupMember)
            groupMember.accountGroup
        }

        return removedFromAccountGroups
    }
}
