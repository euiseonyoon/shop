package com.example.shop.auth.common

import com.example.shop.auth.domain.Authority
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component

@Component
class TestAuthorityFactory {
    fun createAuthorities(em: EntityManager, roleNames: List<String>): List<Authority> {
        val query = em.createQuery("SELECT a FROM Authority a WHERE a.roleName = :roleName", Authority::class.java)

        val authorities = roleNames.map { name ->
            val results = query.setParameter("roleName", name).resultList
            if (results.isEmpty()) {
                Authority(name).also { em.persist(it) }
            } else {
                results.first()
            }
        }
        em.flush()
        return authorities
    }
}
