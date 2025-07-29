package com.example.shop.auth.services

import com.example.shop.auth.domain.Account
import com.example.shop.auth.domain.AccountGroup
import com.example.shop.auth.domain.GroupMember
import com.example.shop.auth.repositories.GroupMemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GroupMemberService(
    private val groupMemberRepository: GroupMemberRepository,
) {
    @Transactional
    fun setAccountGroup(
        account: Account,
        groups: Set<AccountGroup>
    ): List<GroupMember> {
        return groups.map { group ->
            val groupMember = GroupMember(account, group)
            val savedGroupMember = groupMemberRepository.save(groupMember)
            savedGroupMember
        }
    }
}
