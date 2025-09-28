package com.example.shop.auth.models

import com.example.shop.auth.domain.Role


class AuthRequest {
    data class AccountGroupRequest(
        val groupIds: Set<Long>,
        val assignGroupStrictly: Boolean,
    )

    data class RoleRequest(
        val role: Role,
        val roleHierarchy: Int,
        val createIfNotExist: Boolean,
    )
}

typealias RoleRequest = AuthRequest.RoleRequest
typealias AccountGroupRequest = AuthRequest.AccountGroupRequest
