package com.example.shop.auth.security.events.publishers

import com.example.shop.auth.security.events.interfaces.AutoRegisteredAccountEventPublisher
import com.example.shop.auth.security.events.models.AutoRegisteredAccountEvent
import com.example.shop.auth.security.third_party.models.AccountFindOrCreateResult
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class AutoRegisteredAccountEventPublisherImpl(
    private val eventPublisher: ApplicationEventPublisher
): AutoRegisteredAccountEventPublisher {
    override fun publishAutoRegisteredEvent(createdResult: AccountFindOrCreateResult) {
        if (createdResult.newlyCreated) {
            val event = AutoRegisteredAccountEvent(createdResult.account.username!!, createdResult.generatedPassword!!)
            eventPublisher.publishEvent(event)
        }
    }
}
