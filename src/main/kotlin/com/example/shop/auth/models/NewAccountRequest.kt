package com.example.shop.auth.models

import com.example.shop.auth.domain.RoleName


class NewAccountRequest {
    data class AccountGroupRequest(
        val groupIds: Set<Long>,
        val assignGroupStrictly: Boolean,
    )

    data class RoleRequest(
        val roleName: RoleName,
        val roleHierarchy: Int,
        val createIfNotExist: Boolean,
    )
}
