package com.example.shop.redis.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Component

@Component
class RedisOperatorHelper(
    private val json: Json
) {
    fun <T> serialize(value: T, serializer: KSerializer<T>): ByteArray {
        return json.encodeToString(serializer, value).toByteArray(Charsets.UTF_8)
    }

    fun <T> deserialize(bytes: ByteArray?, serializer: KSerializer<T>): T? {
        return bytes?.let {
            json.decodeFromString(serializer, it.toString(Charsets.UTF_8))
        }
    }

    fun <P> generateKey(keyGenerator: RedisKeyGenerator<P>, keyParam: P): String {
        return keyGenerator.generate(keyParam)
    }
}
