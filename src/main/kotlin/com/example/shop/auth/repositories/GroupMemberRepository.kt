package com.example.shop.auth.repositories

import com.example.shop.auth.domain.GroupMember
import com.example.shop.auth.repositories.extensions.GroupMemberRepositoryExtension
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface GroupMemberRepository : JpaRepository<GroupMember, Long>, GroupMemberRepositoryExtension {

}
