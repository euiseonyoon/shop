package com.example.shop.auth.repositories

import com.example.shop.auth.domain.AccountGroup
import org.springframework.data.jpa.repository.JpaRepository

interface AccountGroupRepository : JpaRepository<AccountGroup, Long> {
    fun findByNameIn(names: List<String>): List<AccountGroup>
}
