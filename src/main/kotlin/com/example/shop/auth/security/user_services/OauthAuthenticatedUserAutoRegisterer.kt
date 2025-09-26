package com.example.shop.auth.security.user_services

import com.example.shop.auth.security.third_party.enums.ThirdPartyAuthenticationVendor
import com.example.shop.auth.security.third_party.interfaces.OauthAuthenticationToAutoRegister
import com.example.shop.auth.security.third_party.models.AccountFindOrCreateResult
import com.example.shop.auth.security.utils.PasswordGenerator
import com.example.shop.auth.services.AccountService
import com.example.shop.auth.services.facades.FacadeAccountCrudService
import com.example.shop.kafka.KafkaMessageSender
import com.example.shop.kafka.notify_topic.enums.NotifyType
import com.example.shop.kafka.notify_topic.models.NotifyKafkaContent
import com.example.shop.kafka.notify_topic.models.NotifyKafkaMessage
import org.springframework.stereotype.Service

@Service
class OauthAuthenticatedUserAutoRegisterer(
    private val accountService: AccountService,
    private val facadeAccountCrudService: FacadeAccountCrudService,
    private val passwordGenerator: PasswordGenerator,
    private val kafkaMessageSender: KafkaMessageSender,
) : OauthAuthenticationToAutoRegister {
    override fun generatePassword(length: Int?): String {
        return passwordGenerator.generatePassword(length)
    }

    override fun sendAutoRegisteredAccountKafkaMessage(newUserInfo: AccountFindOrCreateResult) {
        kafkaMessageSender.sendNotifyMessage(
            NotifyKafkaMessage(
                type = NotifyType.AUTO_REGISTERED_ACCOUNT,
                content = NotifyKafkaContent.AutoRegisteredAccountKafkaDto(
                    email = newUserInfo.account.email!!,
                    rawPassword = newUserInfo.account.passwordHash!!
                )
            )
        )
    }

    override fun findOrCreateUser(
        email: String,
        providerId: ThirdPartyAuthenticationVendor,
    ): AccountFindOrCreateResult {
        val accountFromDb = accountService.findWithAuthoritiesByEmail(email)
        if (accountFromDb != null) {
            return AccountFindOrCreateResult(accountFromDb, null, false)
        }
        val generatedPassword = generatePassword()

        val createdAccount =
            facadeAccountCrudService.createUserAccount(email, generatedPassword, null, providerId, emptySet())

        return AccountFindOrCreateResult(createdAccount, generatedPassword, true)
            .also { sendAutoRegisteredAccountKafkaMessage(it) }
    }
}
