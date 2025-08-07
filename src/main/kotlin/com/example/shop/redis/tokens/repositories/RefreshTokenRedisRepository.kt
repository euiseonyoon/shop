package com.example.shop.redis.tokens.repositories

import com.example.shop.constants.REFRESH_TOKEN_EXPIRATION_MS
import com.example.shop.redis.repositories.GenericRedisRepository
import com.example.shop.redis.tokens.utils.RefreshTokenRedisKeyGenerator
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import kotlin.time.Duration.Companion.microseconds

@Repository
class RefreshTokenRedisRepository(
    redisTemplate: RedisTemplate<String, ByteArray>,
    redisKeyGenerator: RefreshTokenRedisKeyGenerator,
    json: Json,
) : GenericRedisRepository<Long, String>(
    redisTemplate = redisTemplate,
    redisKeyGenerator = redisKeyGenerator,
    valueSerializer = String.serializer(),
    defaultTtl = REFRESH_TOKEN_EXPIRATION_MS.microseconds,
    json = json,
)
