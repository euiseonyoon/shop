package com.example.shop.auth.services

import com.example.shop.auth.domain.Account
import com.example.shop.auth.domain.AccountDomain
import com.example.shop.auth.domain.AccountGroup
import com.example.shop.auth.domain.GroupMember
import com.example.shop.auth.exceptions.AccountGroupPartiallyNotFoundException
import com.example.shop.auth.models.NewAccountRequest
import com.example.shop.auth.repositories.AccountRepository
import com.example.shop.auth.repositories.GroupAuthorityRepository
import com.example.shop.auth.repositories.GroupMemberRepository
import com.example.shop.auth.security.third_party.enums.ThirdPartyAuthenticationVendor
import com.example.shop.common.apis.models.AccountSearchCriteria
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccountDomainService(
    private val accountRepository: AccountRepository,
    private val accountService: AccountService,
    private val groupAuthorityRepository: GroupAuthorityRepository,
    private val groupMemberRepository: GroupMemberRepository,
    private val authorityService: AuthorityService,
) {
    @Transactional
    fun newAccountDomain(
        email: String,
        rawPassword: String,
        nickname: String?,
        thirdPartyOauthVendor: ThirdPartyAuthenticationVendor?,
        roleRequest: NewAccountRequest.RoleRequest,
        groupRequest: NewAccountRequest.AccountGroupRequest,
    ): AccountDomain {
        val authority = authorityService.getOrCreateAuthority(roleRequest)
        val newAccount = accountService.createAccount(email, rawPassword, nickname, thirdPartyOauthVendor, authority)

        // 유저를 `AccountGroup`에 가입시키는 과정 (`GroupMember` row 생성)
        val groupAndGroupAuthorityMap = groupAuthorityRepository.findAllByAccountGroupIdIn(
            groupRequest.groupIds.toList()
        ).groupBy { it.accountGroup }
        validateAccountGroups(groupRequest.assignGroupStrictly, groupRequest.groupIds, groupAndGroupAuthorityMap.keys)
        groupAndGroupAuthorityMap.keys.map {
            // DB에 `GroupMember` row 들 생성
            groupMemberRepository.save(GroupMember(it, newAccount))
        }

        return AccountDomain(
            account = newAccount,
            accountGroupMap = groupAndGroupAuthorityMap.toMutableMap()
        )
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
    fun findByEmail(email: String): AccountDomain? {
        val account = accountRepository.findByEmail(email) ?: return null

        return AccountDomain(
            account,
            groupAuthorityRepository.getAccountGroupAuthorities(account.id)
                .groupBy { it.accountGroup }.toMutableMap()
        )
    }

    @Transactional(readOnly = true)
    fun findByAccountId(accountId: Long): AccountDomain? {
        val account = accountRepository.findById(accountId).orElse(null) ?: return null
        return AccountDomain(
            account,
            groupAuthorityRepository.getAccountGroupAuthorities(account.id)
                .groupBy { it.accountGroup }.toMutableMap()
        )
    }

    @Transactional
    fun findAllByAccountIdIn(accountIds: List<Long>): List<AccountDomain> {
        return groupAuthorityRepository.getAccountGroupAuthorityDtos(accountIds)
            .groupBy { it.account }
            .map { (account, dtoList) ->
                AccountDomain(
                    account = account,
                    accountGroupMap = dtoList.map { it.groupAuthority }.groupBy { it.accountGroup }.toMutableMap()
                )
            }
    }

    @Transactional
    fun saveAccount(account: Account): Account {
        return accountRepository.save(account)
    }

    @Transactional(readOnly = true)
    fun searchWithCriteria(criteria: AccountSearchCriteria): Page<AccountDomain> {
        val accountResult = accountService.searchWithCriteria(criteria)
        val content = accountResult.content.map { it.id }.let {
            findAllByAccountIdIn(it)
        }
        return PageImpl(content, criteria.pageable, accountResult.totalElements)
    }
}
