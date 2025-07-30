package com.example.shop.auth.security.third_party.models

import com.example.shop.auth.domain.Account

open class AccountFindOrCreateResult(
    val account: Account,
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
