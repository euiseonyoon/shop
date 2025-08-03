package com.example.shop.redis.repositories

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlin.time.Duration


interface RedisRepository<KeyParamType, ValueType> {
    val defaultTtl: Duration?
    val valueSerializer: KSerializer<ValueType>
    val json: Json

    fun save(keyParam: KeyParamType, value: ValueType, ttl: Duration? = null)
    fun find(keyParam: KeyParamType): ValueType?
    fun delete(keyParam: KeyParamType)

    fun serialize(value: ValueType): ByteArray {
        return json.encodeToString(valueSerializer, value).toByteArray(Charsets.UTF_8)
    }

    fun deserialize(bytes: ByteArray?): ValueType? {
        return bytes?.let {
            json.decodeFromString(valueSerializer, it.toString(Charsets.UTF_8))
        }
    }
}
