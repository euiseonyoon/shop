package com.example.shop.redis.authority_refresh

import com.example.shop.auth.utils.RoleHierarchyHelper
import com.example.shop.common.logger.LogSupport


class AuthorityRefreshMessageSubscriber(
    private val roleHierarchyHelper: RoleHierarchyHelper
): LogSupport() {
    // `RoleHierarchyRedisPubSubConfig`의 listenerAdapter 빈에서 호출할 메서드
    fun handleAuthorityRefreshMessage(message: String, channel: String) {
        logger.info("Authority(role) refresh event from redis. channel='$channel', message='$message'")
        roleHierarchyHelper.refreshRoleMap()
    }
}
