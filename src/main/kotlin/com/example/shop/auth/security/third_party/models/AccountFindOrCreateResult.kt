package com.example.shop.auth.security.third_party.models

import com.example.shop.auth.domain.Account

data class AccountFindOrCreateResult(
    val account: Account,
    val generatedPassword: String?,
    val newlyCreated: Boolean,
) {
    init {
        requireNotNull(account.username) { "If Account email must be provided." }

        if (newlyCreated) {
            requireNotNull(generatedPassword) {
                "If Account is newly created, generated password must be provided."
            }
        }
    }
}
