package com.example.shop.configurations.rate_limit

import com.example.shop.auth.security.rate_limit.models.RateLimitProperties
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager
import io.lettuce.core.RedisClient
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.codec.StringCodec
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Bucket4jConfig {
    @Bean
    fun redisClient(
        @Value("\${spring.data.redis.host}") host: String,
        @Value("\${spring.data.redis.port}") port: Int
    ): RedisClient {
        val redisUrl = "redis://$host:$port"
        return RedisClient.create(redisUrl)
    }

    @Bean
    fun bucket4jProxyManager(
        redisClient: RedisClient,
        rateLimitProperties: RateLimitProperties
    ): LettuceBasedProxyManager<String> {
        val codec: RedisCodec<String, ByteArray> = RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE)
        val connection = redisClient.connect(codec)
        // 버킷이 다 차도 n초 동안은 레디스에 버킷유지 하여 api rate limit 추적한다.
        val keepAfterRefillDuration = rateLimitProperties.keepAfterRefill

        return LettuceBasedProxyManager
            .builderFor(connection)
            .withExpirationStrategy(
                ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(keepAfterRefillDuration)
            ).build()
    }
}
