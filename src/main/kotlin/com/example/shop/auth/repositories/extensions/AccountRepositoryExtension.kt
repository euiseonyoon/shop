package com.example.shop.auth.repositories.extensions

import com.example.shop.auth.domain.Account

interface AccountRepositoryExtension {
    fun findWithAuthoritiesByEmail(username: String): Account?
}
