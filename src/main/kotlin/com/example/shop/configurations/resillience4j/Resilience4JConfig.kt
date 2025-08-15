package com.example.shop.configurations.resillience4j

import com.example.shop.constants.REDIS_OPEN_TO_HALF_OPEN_TIME
import com.example.shop.constants.REDIS_CIRCUIT_BREAKER
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration


// ref: https://resilience4j.readme.io/docs/examples
@Configuration
class Resilience4JConfig {
    @Bean
    fun redisCircuitBreakerConfig(): CircuitBreakerConfig {
        return CircuitBreakerConfig
            .custom()
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(20)
            // .minimumNumberOfCalls(20) 따로 설정하지 않으면 slidingWindowSize와 동일, 최소 20개의 call이 만들어진후에 circuit breaker의 state 변경
            .failureRateThreshold(5F)
            .slowCallRateThreshold(5F)
            .slowCallDurationThreshold(Duration.ofMillis(500))
            // The time that the CircuitBreaker should wait before transitioning from open to half-open.
            .waitDurationInOpenState(REDIS_OPEN_TO_HALF_OPEN_TIME)
            // If set to true it means that the CircuitBreaker will automatically transition
            // from open to half-open state and no call is needed to trigger the transition.
            // REDIS_OPEN_TO_HALF_OPEN_TIME(1초) 후에 자동적으로 OPEN -> HALF_OPEN으로 변경
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            // Configures the number of permitted calls when the CircuitBreaker is half open.
            .permittedNumberOfCallsInHalfOpenState(20)
            .build()
    }

    @Bean
    fun redisCircuitBreakerRegistry(): CircuitBreakerRegistry {
        return CircuitBreakerRegistry.of(redisCircuitBreakerConfig())
    }

    // create a circuit breaker
    @Bean
    fun redisCircuitBreaker(): CircuitBreaker {
        return redisCircuitBreakerRegistry().circuitBreaker(REDIS_CIRCUIT_BREAKER)
    }
}
