package com.example.shop.auth.common

import com.example.shop.auth.domain.Authority
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component

@Component
class TestAuthorityFactory {
    fun createAuthorities(em: EntityManager, roleInfo: List<Pair<String, Int>>): List<Authority> {
        val query = em.createQuery("SELECT a FROM Authority a WHERE a.roleName = :roleName", Authority::class.java)

        val authorities = roleInfo.map { (name, hierarchy) ->
            val results = query.setParameter("roleName", name).resultList
            if (results.isEmpty()) {
                Authority(name, hierarchy).also { em.persist(it) }
            } else {
                results.first()
            }
        }
        em.flush()
        return authorities
    }
}
