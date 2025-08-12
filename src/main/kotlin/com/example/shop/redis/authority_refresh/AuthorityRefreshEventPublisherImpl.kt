package com.example.shop.redis.authority_refresh

import com.example.shop.common.logger.LogSupport
import com.example.shop.constants.REDIS_AUTHORITY_REFRESH_CHANNEL
import com.example.shop.constants.REDIS_CIRCUIT_BREAKER
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.springframework.data.redis.core.RedisTemplate

open class AuthorityRefreshEventPublisherImpl(
    private val redisTemplate: RedisTemplate<String, ByteArray>,
): AuthorityRefreshEventPublisher, LogSupport() {
    @CircuitBreaker(name = REDIS_CIRCUIT_BREAKER, fallbackMethod = "fallBackIfFailedToPublishEvent")
    override fun publishAuthorityRefreshEvent(message: String) {
        redisTemplate.convertAndSend(REDIS_AUTHORITY_REFRESH_CHANNEL, message.toByteArray())
    }

    override fun fallBackIfFailedToPublishEvent(message: String, e: Throwable) {
        logger.error("Failed to publish authority(role) update event. message={}, error={}", message, e)
    }
}
