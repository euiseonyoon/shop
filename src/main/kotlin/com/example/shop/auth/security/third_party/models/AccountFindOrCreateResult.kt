package com.example.shop.auth.security.third_party.models

import com.example.shop.auth.domain.AccountDomain

data class AccountFindOrCreateResult(
    val accountDomain: AccountDomain,
    val generatedPassword: String?,
    val newlyCreated: Boolean,
) {
    init {
        if (newlyCreated) {
            requireNotNull(generatedPassword) {
                "If Account is newly created, generated password must be provided."
            }
        }
    }
}
