package com.example.shop.auth.common

import com.example.shop.auth.domain.AccountGroup
import com.example.shop.auth.domain.GroupAuthority
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component

@Component
class TestGroupAuthorityFactory {
    fun createGroupAuthorities(em: EntityManager, names: List<String>, group: AccountGroup): List<GroupAuthority> {
        val groupAuthorities = names.map { name ->
            val ga = GroupAuthority(name, group)
            group.addGroupAuthority(ga).also { em.persist(ga) }
            ga
        }
        em.flush()
        return groupAuthorities
    }
}
