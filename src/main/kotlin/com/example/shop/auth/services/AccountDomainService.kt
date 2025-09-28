package com.example.shop.auth.services

import com.example.shop.auth.domain.Account
import com.example.shop.auth.domain.AccountDomain
import com.example.shop.auth.domain.AccountGroup
import com.example.shop.auth.domain.GroupAuthority
import com.example.shop.auth.models.AccountGroupRequest
import com.example.shop.auth.models.RoleRequest
import com.example.shop.auth.repositories.AccountRepository
import com.example.shop.auth.repositories.GroupAuthorityRepository
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
    private val authorityDomainService: AuthorityDomainService,
) {
    @Transactional
    fun newAccountDomain(
        email: String,
        rawPassword: String,
        nickname: String?,
        thirdPartyOauthVendor: ThirdPartyAuthenticationVendor?,
        roleRequest: RoleRequest,
        groupRequest: AccountGroupRequest,
    ): AccountDomain {
        val authority = authorityDomainService.getOrCreateAuthority(roleRequest)
        val newAccount = accountService.createAccount(email, rawPassword, nickname, thirdPartyOauthVendor, authority)

        val accountGroupAndAuthorityMap = authorityDomainService.addAccountToAccountGroups(newAccount, groupRequest)

        return AccountDomain(
            account = newAccount,
            accountGroupMap = accountGroupAndAuthorityMap.toMutableMap()
        )
    }

    @Transactional(readOnly = true)
    fun findByEmail(email: String): AccountDomain? {
        val account = accountRepository.findByEmail(email) ?: return null
        return AccountDomain(
            account,
            authorityDomainService.getGroupAuthoritiesByAccountId(account.id).let {
                groupByAccountGroup(it)
            }
        )
    }

    @Transactional(readOnly = true)
    fun findByAccountId(accountId: Long): AccountDomain? {
        val account = accountRepository.findById(accountId).orElse(null) ?: return null
        return AccountDomain(
            account,
            groupAuthorityRepository.getAccountGroupAuthorities(account.id).let {
                groupByAccountGroup(it)
            }
        )
    }

    private fun groupByAccountGroup(
        groupAuthorities: List<GroupAuthority>
    ) : MutableMap<AccountGroup, List<GroupAuthority>>{
        return groupAuthorities.groupBy { it.accountGroup }.toMutableMap()
    }


    @Transactional
    fun findAllByAccountIdIn(accountIds: List<Long>): List<AccountDomain> {
        val groupedByAccountMap = authorityDomainService.getGroupAuthoritiesByAccountIds(accountIds).let {
            it.mapValues { (_, groupAuthorities) -> groupByAccountGroup(groupAuthorities) }
        }
        return groupedByAccountMap.map { (account, authoritiesGroupedByAccountGroup) ->
            AccountDomain(account, authoritiesGroupedByAccountGroup)
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
