package com.example.shop.auth.repositories

import com.example.shop.auth.domain.Authority
import org.springframework.data.jpa.repository.JpaRepository

interface AuthorityRepository : JpaRepository<Authority, Long> {
    // roleName 은 unique
    fun findByRoleName(roleName: String): Authority?
}
