package com.example.shop.rate_limit

import com.example.shop.auth.security.filters.RateLimitFilter
import com.example.shop.auth.security.rate_limit.RedisRateLimitHelper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.serialization.json.Json
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
class RateLimitTestConfig {

    class RateLimitFilterWrapper(
        json: Json,
        redisRateLimitHelper: RedisRateLimitHelper,
    ) : RateLimitFilter(json, redisRateLimitHelper) {
        public override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
            super.doFilterInternal(request, response, filterChain)
        }
    }

    @Bean
    fun testRateLimitFilter(
        json: Json,
        redisRateLimitHelper: RedisRateLimitHelper,
    ): RateLimitFilterWrapper {
        return RateLimitFilterWrapper(json, redisRateLimitHelper)
    }
}
