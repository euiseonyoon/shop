package com.example.shop.auth.security.events.interfaces

import com.example.shop.auth.security.events.models.AutoRegisteredAccountEvent

interface AutoRegisteredAccountEventListener {
    fun handleAutoRegisteredAccountEvent(event: AutoRegisteredAccountEvent)
}
