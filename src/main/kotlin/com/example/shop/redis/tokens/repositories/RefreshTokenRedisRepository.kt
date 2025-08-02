package com.example.shop.redis.tokens.repositories

import com.example.shop.auth.REFRESH_TOKEN_EXPIRATION_MS
import com.example.shop.redis.repositories.GenericRedisRepository
import com.example.shop.redis.tokens.utils.RefreshTokenRedisKeyGenerator
import com.example.shop.redis.utils.RedisOperatorHelper
import kotlinx.serialization.builtins.serializer
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import kotlin.time.Duration.Companion.microseconds

@Repository
class RefreshTokenRedisRepository(
    redisTemplate: RedisTemplate<String, ByteArray>,
    redisOperatorHelper: RedisOperatorHelper,
    redisKeyGenerator: RefreshTokenRedisKeyGenerator,
) : GenericRedisRepository<String, String>(
    redisTemplate = redisTemplate,
    redisKeyGenerator = redisKeyGenerator,
    redisOperatorHelper = redisOperatorHelper,
    _serializer = String.serializer(),
    _defaultTtl = REFRESH_TOKEN_EXPIRATION_MS.microseconds,
)
