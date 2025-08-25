package com.example.shop.auth.security.filters

import com.example.shop.auth.security.rate_limit.RedisRateLimitHelper
import com.example.shop.common.response.GlobalResponse
import com.example.shop.constants.NO_API_LIMIT_END_POINTS
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter

@Component
class RateLimitFilter(
    private val json: Json,
    private val redisRateLimitHelper: RedisRateLimitHelper,
): OncePerRequestFilter() {
    private val pathMatcher = AntPathMatcher()

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        val result = NO_API_LIMIT_END_POINTS.any {
            pathMatcher.match(it, path)
        }
        return result
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val bucket = redisRateLimitHelper.resolveBucket(request)

        if (bucket.tryConsume(redisRateLimitHelper.getTokensToConsume(request))) {
            filterChain.doFilter(request, response)
        } else {
            val desiredHttpStatus = HttpStatus.TOO_MANY_REQUESTS
            response.status = desiredHttpStatus.value()
            // 임시로 response type을 `String`으로 하였다. 어차피 GlobalResponse.result = null 이기 때문에 전혀 문제 없다.
            val resBody = GlobalResponse.createErrorRes<String>(response, "Too Many Requests", desiredHttpStatus)
            val jsonResponse = json.encodeToString(GlobalResponse.serializer(String.serializer()), resBody)
            response.writer.write(jsonResponse)
            response.writer.flush()
        }
    }
}
