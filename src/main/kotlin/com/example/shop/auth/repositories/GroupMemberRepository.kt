package com.example.shop.auth.repositories

import com.example.shop.auth.domain.GroupMember
import org.springframework.data.jpa.repository.JpaRepository

interface GroupMemberRepository : JpaRepository<GroupMember, Long>
