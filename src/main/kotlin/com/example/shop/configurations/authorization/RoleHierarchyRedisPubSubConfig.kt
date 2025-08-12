package com.example.shop.configurations.authorization

import com.example.shop.auth.utils.RoleHierarchyHelper
import com.example.shop.constants.REDIS_AUTHORITY_REFRESH_CHANNEL
import com.example.shop.redis.authority_refresh.AuthorityRefreshEventPublisher
import com.example.shop.redis.authority_refresh.AuthorityRefreshEventPublisherImpl
import com.example.shop.redis.authority_refresh.AuthorityRefreshMessageSubscriber
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter

@Configuration
class RoleHierarchyRedisPubSubConfig {
    // Redis 메시지 리스너 컨테이너를 생성.
    @Bean
    fun redisContainer(
        connectionFactory: RedisConnectionFactory,
        listenerAdapter: MessageListenerAdapter
    ): RedisMessageListenerContainer {
        val container = RedisMessageListenerContainer()
        container.setConnectionFactory(connectionFactory)
        // messageListenerAdapter 빈을 'authority-refresh-channel' 에 바인딩합니다.
        container.addMessageListener(listenerAdapter, ChannelTopic(REDIS_AUTHORITY_REFRESH_CHANNEL))
        return container
    }

    @Bean
    fun authorityRefreshMessageSubscriber(
        roleHierarchyHelper: RoleHierarchyHelper,
    ): AuthorityRefreshMessageSubscriber {
        return AuthorityRefreshMessageSubscriber(roleHierarchyHelper)
    }

    @Bean
    fun listenerAdapter(subscriber: AuthorityRefreshMessageSubscriber): MessageListenerAdapter {
        return MessageListenerAdapter(subscriber, "handleAuthorityRefreshMessage")
    }

    @Bean
    fun authorityRefreshEventPublisher(
        redisTemplate: RedisTemplate<String, ByteArray>,
    ): AuthorityRefreshEventPublisher {
        return AuthorityRefreshEventPublisherImpl(redisTemplate)
    }
}
