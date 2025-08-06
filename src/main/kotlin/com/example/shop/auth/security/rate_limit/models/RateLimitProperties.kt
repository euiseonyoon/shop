package com.example.shop.auth.security.rate_limit.models

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.time.Duration

@Component
@ConfigurationProperties(prefix = "rate-limit")
data class RateLimitProperties(
    var capacity: Long = 10,
    var refillTokens: Long = 1,
    var refillPeriod: Duration = Duration.ofSeconds(1),
    var keepAfterRefill: Duration = Duration.ofSeconds(10),
    // 동작이 heavy 하다고 간주되는 api 엔드포인트들의 토큰 소모량  ( api request 방식 to api endpoint 패턴)
    var heavyMap: Map<String, List<HeavyRateLimitUriToken>> = emptyMap()
)
