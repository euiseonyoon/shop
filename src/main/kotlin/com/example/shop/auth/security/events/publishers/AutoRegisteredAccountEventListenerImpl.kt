package com.example.shop.auth.security.events.publishers

import com.example.shop.auth.security.events.interfaces.AutoRegisteredAccountEventListener
import com.example.shop.auth.security.events.models.AutoRegisteredAccountEvent
import com.example.shop.common.logger.LogSupport
import org.springframework.stereotype.Service

@Service
class AutoRegisteredAccountEventListenerImpl : AutoRegisteredAccountEventListener, LogSupport() {
    // TODO: 아래 핸들러를 통해 이메일을 보내거나, 혹은 task를 만들어서 queue에 보내거나 할 수 있다.
    override fun handleAutoRegisteredAccountEvent(event: AutoRegisteredAccountEvent) {
        logger.info("oauth를 통한 유저 register. email: ${event.email}, password: ${event.rawPassword}")
    }
}
