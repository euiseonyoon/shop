package com.example.shop.auth.security.third_party.interfaces

import com.example.shop.auth.security.third_party.models.AccountFindOrCreateResult

interface AutoRegisteredAccountEventPublisher {
    fun publishAutoRegisteredEvent(createdResult: AccountFindOrCreateResult)
}
