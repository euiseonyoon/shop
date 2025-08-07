package com.example.shop.auth.repositories

import com.example.shop.auth.domain.GroupAuthority
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface GroupAuthorityRepository : JpaRepository<GroupAuthority, Long> {
    @Query("SELECT ga FROM GroupAuthority ga LEFT JOIN FETCH ga.accountGroup")
    override fun findAll(pageable: Pageable): Page<GroupAuthority>
}
