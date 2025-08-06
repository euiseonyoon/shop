package com.example.shop.auth.security.filters

import com.example.shop.auth.security.rate_limit.RedisRateLimitHelper
import com.example.shop.common.apis.GlobalResponse
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class RateLimitFilter(
    private val json: Json,
    private val redisRateLimitHelper: RedisRateLimitHelper,
): OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val bucket = redisRateLimitHelper.resolveBucket(request)

        if (bucket.tryConsume(redisRateLimitHelper.getTokensToConsume(request))) {
            filterChain.doFilter(request, response)
        } else {
            response.status = 429
            // 임시로 response type을 `String`으로 하였다. 어차피 GlobalResponse.result = null 이기 때문에 전혀 문제 없다.
            val resBody = GlobalResponse.createErrorRes<String>(response, "Too Many Requests", HttpStatus.TOO_MANY_REQUESTS)
            val jsonResponse = json.encodeToString(GlobalResponse.serializer(String.serializer()), resBody)
            response.writer.write(jsonResponse)
            response.writer.flush()
        }
    }
}
