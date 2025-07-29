package com.example.shop.auth.common

import com.example.shop.auth.ROLE_PREFIX
import com.example.shop.auth.domain.Authority
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component

@Component
class TestAuthorityFactory {
    fun createAuthorities(em: EntityManager, roleNames: List<String>): List<Authority> {
        val authorities = roleNames.map { name ->
            Authority(name).also { em.persist(it) }
        }
        em.flush()
        return authorities
    }
}
