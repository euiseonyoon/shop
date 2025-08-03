package com.example.shop.auth.common

import com.example.shop.auth.domain.AccountGroup
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component

@Component
class TestAccountGroupFactory {
    fun createAccountGroup(em: EntityManager, names: List<String>): List<AccountGroup> {
        val query = em.createQuery("SELECT ag FROM AccountGroup ag WHERE ag.name = :name", AccountGroup::class.java)

        val accountGroups = names.map { name ->
            val results = query.setParameter("name", name).resultList
            if (results.isEmpty()) {
                AccountGroup(name).also { em.persist(it) }
            } else {
                results.first()
            }
        }
        em.flush()
        return accountGroups
    }
}
