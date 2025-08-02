package com.example.shop.redis.utils

interface RedisKeyGenerator<P> {
    val prefix: String
    fun generate(param: P): String
}
