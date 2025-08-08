package com.example.shop.configurations.authorization

import com.example.shop.redis.role_hierarchy.RoleHierarchyMessageSubscriber
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter

@Configuration
class RoleHierarchyRedisPubSubConfig {
    // Redis 메시지 리스너 컨테이너를 생성합니다.
    @Bean
    fun redisContainer(
        connectionFactory: RedisConnectionFactory,
        listenerAdapter: MessageListenerAdapter
    ): RedisMessageListenerContainer {
        val container = RedisMessageListenerContainer()
        container.setConnectionFactory(connectionFactory)
        // messageListenerAdapter 빈을 'role-hierarchy-channel'에 바인딩합니다.
        container.addMessageListener(listenerAdapter, ChannelTopic("role-hierarchy-channel"))
        return container
    }

    @Bean
    fun listenerAdapter(subscriber: RoleHierarchyMessageSubscriber): MessageListenerAdapter {
        return MessageListenerAdapter(subscriber, "handleMessage")
    }
}
