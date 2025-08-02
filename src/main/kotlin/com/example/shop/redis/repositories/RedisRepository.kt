package com.example.shop.redis.repositories

import kotlinx.serialization.KSerializer
import kotlin.time.Duration


interface RedisRepository<T, P> {
    fun save(keyParam: P, value: T, ttl: Duration? = null)
    fun find(keyParam: P): T?
    fun delete(keyParam: P)
    val defaultTtl: Duration?
    val serializer: KSerializer<T>
}
