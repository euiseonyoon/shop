package com.example.shop.redis.repositories

import com.example.shop.redis.utils.RedisKeyGenerator
import com.example.shop.redis.utils.RedisOperatorHelper
import kotlinx.serialization.KSerializer
import org.springframework.data.redis.core.RedisTemplate
import kotlin.time.Duration
import kotlin.time.toJavaDuration


open class GenericRedisRepository<T, P>(
    private val redisTemplate: RedisTemplate<String, ByteArray>,
    private val redisKeyGenerator: RedisKeyGenerator<P>,
    private val redisOperatorHelper: RedisOperatorHelper,
    private val _serializer: KSerializer<T>,
    private val _defaultTtl: Duration,
) : RedisRepository<T, P> {
    override val defaultTtl = _defaultTtl
    override val serializer = _serializer

    override fun save(keyParam: P, value: T, ttl: Duration?) {
        val key = redisOperatorHelper.generateKey(redisKeyGenerator, keyParam)
        val bytes = redisOperatorHelper.serialize(value, serializer)
        redisTemplate.opsForValue().set(key, bytes, (ttl ?: this.defaultTtl).toJavaDuration())
    }

    override fun find(keyParam: P): T? {
        val key = redisKeyGenerator.generate(keyParam)
        val bytes = redisTemplate.opsForValue().get(key) ?: return null
        return redisOperatorHelper.deserialize(bytes, serializer)
    }

    override fun delete(keyParam: P) {
        val key = redisKeyGenerator.generate(keyParam)
        redisTemplate.delete(key)
    }
}
