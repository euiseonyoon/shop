package com.example.shop.auth.common

import com.example.shop.auth.domain.AccountGroup
import com.example.shop.auth.domain.GroupAuthority
import com.example.shop.auth.domain.Role
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Component

@Component
class TestGroupAuthorityFactory {

    fun createGroupAuthorities(em: EntityManager, roles: List<Role>, group: AccountGroup): List<GroupAuthority> {
        val groupAuthorities = roles.map {
            val newGroupAuthority = GroupAuthority(it, group)
            em.persist(newGroupAuthority)
            newGroupAuthority
        }
        em.flush()
        return groupAuthorities
    }
}
