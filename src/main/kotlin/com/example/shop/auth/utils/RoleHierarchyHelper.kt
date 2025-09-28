package com.example.shop.auth.utils

import com.example.shop.auth.domain.Role
import com.example.shop.auth.services.AuthorityService
import com.example.shop.constants.ADMIN_HIERARCHY
import com.example.shop.constants.SUPER_ADMIN_HIERARCHY
import com.example.shop.constants.DEFAULT_USER_HIERARCHY
import com.example.shop.constants.ROLE_ADMIN
import com.example.shop.constants.ROLE_SUPER_ADMIN
import com.example.shop.constants.ROLE_USER
import org.springframework.stereotype.Component

@Component
class RoleHierarchyHelper(
    private val authorityService: AuthorityService
) {
    private lateinit var roleHierarchyMap: Map<Role, Int>
    private lateinit var roleNamesHierarchyAsc: List<Role>

    init { set() }

    fun refreshRoleMap() { set() }

    fun set() {
        val rolesFromDb = authorityService.findAllByHierarchyAscOrdered()

        val hierarchyMap = rolesFromDb.associate { it.role to it.hierarchy }.toMutableMap()

        // SUPER_ADMIN, ADMIN 우선순위는 항상 static 하게 고정한다.
        hierarchyMap[Role(ROLE_SUPER_ADMIN)] = SUPER_ADMIN_HIERARCHY
        hierarchyMap[Role(ROLE_ADMIN)] = ADMIN_HIERARCHY

        if (hierarchyMap[Role(ROLE_USER)] == null) {
            hierarchyMap[Role(ROLE_USER)] = DEFAULT_USER_HIERARCHY
        }

        roleHierarchyMap = hierarchyMap
        roleNamesHierarchyAsc = hierarchyMap.entries
            .sortedBy { it.value }
            .map { it.key }
    }

    fun getRoleHierarchyMap(): Map<Role, Int> = roleHierarchyMap

    fun getRoleNamesInHierarchyAsc(): List<Role> = roleNamesHierarchyAsc

    fun getRoleHierarchy(role: Role): Int? {
        return roleHierarchyMap[role]
    }
}
