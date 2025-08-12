package com.example.shop.configurations.rate_limit

import com.example.shop.auth.security.rate_limit.models.RateLimitProperties
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.codec.StringCodec
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory

@Configuration
class Bucket4jConfig(
    private val redisConnectionFactory: RedisConnectionFactory
) {
    @Bean
    fun statefulLettuceConnection(): StatefulRedisConnection<String, ByteArray> {
        val lettuceFactory = redisConnectionFactory as? LettuceConnectionFactory
            ?: throw IllegalStateException("Bucket4jConfig requires a LettuceConnectionFactory bean")

        val redisClient: RedisClient = lettuceFactory.getNativeClient() as RedisClient

        // Spring이 관리하는 RedisClient를 사용하여 연결을 생성
        val codec: RedisCodec<String, ByteArray> = RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE)
        return redisClient.connect(codec)
    }

    @Bean
    fun bucket4jProxyManager(
        lettuceConnection: StatefulRedisConnection<String, ByteArray>,
        rateLimitProperties: RateLimitProperties
    ): LettuceBasedProxyManager<String> {
        // 버킷이 다 차도 n초 동안은 레디스에 버킷유지 하여 api rate limit 추적한다.
        val keepAfterRefillDuration = rateLimitProperties.keepAfterRefill

        return LettuceBasedProxyManager
            .builderFor(lettuceConnection)
            .withExpirationStrategy(
                ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(keepAfterRefillDuration)
            ).build()
    }
}
