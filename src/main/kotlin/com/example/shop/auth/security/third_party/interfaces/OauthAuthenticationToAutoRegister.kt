package com.example.shop.auth.security.third_party.interfaces

import com.example.shop.auth.domain.Email
import com.example.shop.auth.security.third_party.enums.ThirdPartyAuthenticationVendor
import com.example.shop.auth.security.third_party.models.AccountFindOrCreateResult

interface OauthAuthenticationToAutoRegister {
    fun findOrCreateUser(email: Email, providerId: ThirdPartyAuthenticationVendor): AccountFindOrCreateResult

    fun generatePassword(length: Int? = null): String

    fun sendAutoRegisteredAccountKafkaMessage(newUserInfo: AccountFindOrCreateResult)
}
