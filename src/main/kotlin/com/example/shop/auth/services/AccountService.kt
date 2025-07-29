package com.example.shop.auth.services

import com.example.shop.auth.domain.Account
import com.example.shop.auth.repositories.AccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccountService(
    private val accountRepository: AccountRepository
) {
    @Transactional(readOnly = true)
    fun findWithAuthoritiesByEmail(email: String): Account? {
        return accountRepository.findWithAuthoritiesByEmail(email)
    }
}
