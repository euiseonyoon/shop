package com.example.shop.redis.authority_refresh

import com.example.shop.constants.REDIS_AUTHORITY_REFRESH_CHANNEL
import org.springframework.data.redis.core.RedisTemplate

class AuthorityRefreshEventPublisher(
    private val redisTemplate: RedisTemplate<String, ByteArray>,
) {
    fun publishAuthorityRefreshEvent(message: String) {
        redisTemplate.convertAndSend(REDIS_AUTHORITY_REFRESH_CHANNEL, message.toByteArray())
    }
}
