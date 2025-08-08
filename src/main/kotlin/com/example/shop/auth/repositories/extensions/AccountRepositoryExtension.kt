package com.example.shop.auth.repositories.extensions

import com.example.shop.auth.domain.Account
import com.example.shop.common.apis.models.AccountSearchCriteria
import org.springframework.data.domain.Page

interface AccountRepositoryExtension {
    fun findWithAuthoritiesByEmail(email: String): Account?

    fun findWithAuthoritiesById(accountId: Long): Account?

    fun findWithCriteria(criteria: AccountSearchCriteria): Page<Account>
}
