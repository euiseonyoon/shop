package com.example.shop.auth.security.user_services

import com.example.shop.auth.domain.Email
import com.example.shop.auth.security.third_party.enums.ThirdPartyAuthenticationVendor
import com.example.shop.auth.security.third_party.interfaces.OauthAuthenticationToAutoRegister
import com.example.shop.auth.security.third_party.models.AccountFindOrCreateResult
import com.example.shop.auth.security.utils.PasswordGenerator
import com.example.shop.auth.services.AccountDomainService
import com.example.shop.auth.services.facades.FacadeAccountCrudService
import com.example.shop.kafka.KafkaMessageSender
import com.example.shop.kafka.notify_topic.enums.NotifyType
import com.example.shop.kafka.notify_topic.models.NotifyKafkaContent
import com.example.shop.kafka.notify_topic.models.NotifyKafkaMessage
import org.springframework.stereotype.Service

@Service
class OauthAuthenticatedUserAutoRegisterer(
    private val accountDomainService: AccountDomainService,
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
                    email = newUserInfo.accountDomain.account.email.address,
                    rawPassword = newUserInfo.accountDomain.account.passwordHash
                )
            )
        )
    }

    override fun findOrCreateUser(
        email: Email,
        providerId: ThirdPartyAuthenticationVendor,
    ): AccountFindOrCreateResult {
        val accountDomainFromDb = accountDomainService.findByEmail(email)
        if (accountDomainFromDb != null) {
            return AccountFindOrCreateResult(accountDomainFromDb, null, false)
        }
        val generatedPassword = generatePassword()

        val newAccountDomain = facadeAccountCrudService.createUserAccount(
            email,
            generatedPassword,
            null,
            providerId,
            emptySet()
        )

        return AccountFindOrCreateResult(newAccountDomain, generatedPassword, true)
            .also { sendAutoRegisteredAccountKafkaMessage(it) }
    }
}
