package com.example.shop.auth.common

import com.example.shop.auth.domain.AccountGroup
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component

@Component
class TestAccountGroupFactory {
    fun createAccountGroup(em: EntityManager, names: List<String>): List<AccountGroup> {
        val accountGroups = names.map { name ->
            AccountGroup(name).also { em.persist(it) }
        }
        em.flush()
        return accountGroups
    }
}
