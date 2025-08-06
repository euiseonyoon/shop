package com.example.shop.auth.security.rate_limit

import com.example.shop.auth.security.rate_limit.models.HeavyRateLimitUriToken
import com.example.shop.auth.security.rate_limit.models.RateLimitProperties
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.distributed.AsyncBucketProxy
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import java.util.function.Supplier


@Component
class RedisRateLimitHelperImpl(
    private val lettuceBasedProxyManager: LettuceBasedProxyManager<String>,
    private val rateLimitProperties: RateLimitProperties
): RedisRateLimitHelper {
    private val pathMatcher = AntPathMatcher()

    private val bucketConfig: BucketConfiguration by lazy {
        val limit = Bandwidth.builder()
            .capacity(rateLimitProperties.capacity)
            .refillGreedy(rateLimitProperties.refillTokens, rateLimitProperties.refillPeriod)
            .build()

        BucketConfiguration.builder().addLimit(limit).build()
    }

    private fun resolveUserKey(request: HttpServletRequest): String {
        val header = request.getHeader("X-Forwarded-For")
        return header?.split(",")?.firstOrNull()?.trim()
            ?: request.remoteAddr
    }

    override fun getBucketConfiguration(): BucketConfiguration = this.bucketConfig

    override fun resolveBucket(request: HttpServletRequest): Bucket {
        val key = resolveUserKey(request)
        /***
         * LettuceBasedProxyManager.isAsyncModeSupported = true,
         * 따라서 io.github.bucket4j.distributed.proxy.ProxyManager.asAsync()를 사용해도 괜찮다.
         *
         * val bucket = lettuceBasedProxyManager.asAsync().builder().build(key, Supplier { CompletableFuture.completedFuture(getBucketConfiguration()) })
         * 이건 webflux나 코루틴 처럼 non-blocking일때 사용하면 좋을듯.
         */
        return lettuceBasedProxyManager.builder().build(key, Supplier { getBucketConfiguration() })
    }

    override fun getTokensToConsume(request: HttpServletRequest): Long {
        val method = request.method.uppercase()
        val path = request.requestURI
        val tokensToConsume = rateLimitProperties.heavyMap[method]?.firstOrNull { urlToken: HeavyRateLimitUriToken ->
            // urlToken.url 은 `/login` 처럼 concrete uri 일수도 있고,
            // `/login/oauth/*`, `/admin/workers/**` 처럼 패턴 일 수도 있음
            pathMatcher.match(urlToken.urlPattern, path)
        }?.tokens ?: 1
        return tokensToConsume.toLong()
    }
}
