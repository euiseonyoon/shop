package com.example.shop.redis.tokens.utils

import com.example.shop.redis.utils.RedisKeyGenerator
import org.springframework.stereotype.Component

@Component
class RefreshTokenRedisKeyGenerator : RedisKeyGenerator<Long> {
    override val prefix = "refresh_token:"

    override fun generate(param: Long): String = prefix + param
}
