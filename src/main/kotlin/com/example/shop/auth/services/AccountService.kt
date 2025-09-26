package com.example.shop.auth.services

import com.example.shop.auth.domain.Account
import com.example.shop.auth.domain.Authority
import com.example.shop.auth.repositories.AccountRepository
import com.example.shop.auth.security.third_party.enums.ThirdPartyAuthenticationVendor
import com.example.shop.common.apis.models.AccountSearchCriteria
import org.springframework.data.domain.Page
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun save(account: Account): Account = accountRepository.save(account)

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
            this.passwordHash = passwordEncoder.encode(rawPassword)
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
}
