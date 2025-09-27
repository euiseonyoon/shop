package com.example.shop.auth.repositories

import com.example.shop.auth.domain.GroupMember
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface GroupMemberRepository : JpaRepository<GroupMember, Long> {
    @Query("SELECT gm FROM GroupMember gm JOIN FETCH gm.accountGroup WHERE WHERE gm.id IN :ids")
    fun findAllByIdIn(ids: List<Long>): List<GroupMember>
}
