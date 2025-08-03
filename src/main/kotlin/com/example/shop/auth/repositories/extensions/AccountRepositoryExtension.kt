package com.example.shop.auth.repositories.extensions

import com.example.shop.auth.domain.Account

interface AccountRepositoryExtension {
    fun findWithAuthoritiesByEmail(email: String): Account?

    fun findWithAuthoritiesById(accountId: Long): Account?
}
