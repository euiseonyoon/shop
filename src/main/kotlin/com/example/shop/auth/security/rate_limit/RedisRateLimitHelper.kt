package com.example.shop.auth.security.rate_limit

import io.github.bucket4j.Bucket
import io.github.bucket4j.BucketConfiguration
import jakarta.servlet.http.HttpServletRequest

interface RedisRateLimitHelper {
    fun resolveBucket(request: HttpServletRequest): Bucket

    fun getTokensToConsume(request: HttpServletRequest): Long

    fun getBucketConfiguration(): BucketConfiguration

    fun getEmptyBucketConfiguration(): BucketConfiguration

    fun fallBackIfBucketFromRedisFailed(request: HttpServletRequest): Bucket
}
