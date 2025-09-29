package com.example.shop.auth.common

import com.example.shop.auth.domain.Authority
import com.example.shop.auth.domain.Role
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component

@Component
class TestAuthorityFactory {
    fun createAuthorities(em: EntityManager, roleInfo: List<Pair<Role, Int>>): List<Authority> {
        val query = em.createQuery("SELECT a FROM Authority a WHERE a.role = :role", Authority::class.java)

        val authorities = roleInfo.map { (role, hierarchy) ->
            val results = query.setParameter("role", role).resultList
            if (results.isEmpty()) {
                Authority(role, hierarchy).also { em.persist(it) }
            } else {
                results.first()
            }
        }
        em.flush()
        return authorities
    }
}
