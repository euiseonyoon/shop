package com.example.shop.redis.repositories

import com.example.shop.redis.utils.RedisKeyGenerator
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.springframework.data.redis.core.RedisTemplate
import kotlin.time.Duration
import kotlin.time.toJavaDuration


open class GenericRedisRepository<KeyParamType, ValueType>(
    private val redisTemplate: RedisTemplate<String, ByteArray>,
    private val redisKeyGenerator: RedisKeyGenerator<KeyParamType>,
    override val valueSerializer: KSerializer<ValueType>,
    override val defaultTtl: Duration,
    override val json: Json
) : RedisRepository<KeyParamType, ValueType> {
    override fun save(keyParam: KeyParamType, value: ValueType, ttl: Duration?) {
        val key = redisKeyGenerator.generate(keyParam)
        val bytes = this.serialize(value)
        redisTemplate.opsForValue().set(key, bytes, (ttl ?: this.defaultTtl).toJavaDuration())
    }

    override fun find(keyParam: KeyParamType): ValueType? {
        val key = redisKeyGenerator.generate(keyParam)
        val bytes = redisTemplate.opsForValue().get(key) ?: return null
        return deserialize(bytes)
    }

    override fun delete(keyParam: KeyParamType) {
        val key = redisKeyGenerator.generate(keyParam)
        redisTemplate.delete(key)
    }
}
