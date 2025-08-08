package com.example.shop.redis.role_hierarchy

import com.example.shop.auth.utils.RoleHierarchyHelper
import org.springframework.stereotype.Component


@Component
class RoleHierarchyMessageSubscriber(
    private val roleHierarchyHelper: RoleHierarchyHelper
) {
    // RedisConfig의 listenerAdapter 빈에서 호출할 메서드입니다.
    // 수신된 메시지를 받아 RoleHierarchyImpl 빈의 계층 구조를 업데이트합니다.
    fun handleMessage(message: String, channel: String) {
        println("Redis로부터 메시지 수신: 채널='$channel', 메시지='$message'")

        // 메시지 내용으로 RoleHierarchyImpl 빈의 계층 구조를 동적으로 업데이트합니다.
        roleHierarchyHelper.set()
        println("역할 계층이 성공적으로 업데이트되었습니다.")
    }
}
