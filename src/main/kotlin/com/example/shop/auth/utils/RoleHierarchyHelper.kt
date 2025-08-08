package com.example.shop.auth.utils

import com.example.shop.auth.services.AuthorityService
import com.example.shop.constants.ADMIN_HIERARCHY
import com.example.shop.constants.SUPER_ADMIN_HIERARCHY
import com.example.shop.constants.DEFAULT_USER_HIERARCHY
import com.example.shop.constants.ROLE_ADMIN
import com.example.shop.constants.ROLE_PREFIX
import com.example.shop.constants.ROLE_SUPER_ADMIN
import com.example.shop.constants.ROLE_USER
import org.springframework.stereotype.Component

@Component
class RoleHierarchyHelper(
    private val authorityService: AuthorityService
) {
    private lateinit var roleHierarchyMap: Map<String, Int>
    private lateinit var roleNamesHierarchyAsc: List<String>

    init {
        set()
    }

    fun set() {
        val roles = authorityService.findAllByHierarchyAsc()

        val hierarchyMap = roles.associate { it.roleName!! to it.hierarchy!! }.toMutableMap()

        // SUPER_ADMIN, ADMIN 우선순위는 항상 static 하게 고정한다.
        hierarchyMap[ROLE_SUPER_ADMIN] = SUPER_ADMIN_HIERARCHY
        hierarchyMap[ROLE_ADMIN] = ADMIN_HIERARCHY

        if (hierarchyMap[ROLE_USER] == null) {
            hierarchyMap[ROLE_USER] = DEFAULT_USER_HIERARCHY
        }

        roleHierarchyMap = hierarchyMap
        roleNamesHierarchyAsc = hierarchyMap.entries
            .sortedBy { it.value }
            .map { it.key }
    }

    fun getRoleHierarchyMap(): Map<String, Int> = roleHierarchyMap

    fun getRoleNamesInHierarchyAsc(): List<String> = roleNamesHierarchyAsc

    fun getRoleHierarchy(roleName: String): Int? {
        require(roleName.startsWith(ROLE_PREFIX))
        return roleHierarchyMap[roleName]
    }
}
