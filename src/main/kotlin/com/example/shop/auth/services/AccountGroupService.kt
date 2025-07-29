package com.example.shop.auth.services

import com.example.shop.auth.domain.AccountGroup
import com.example.shop.auth.repositories.AccountGroupRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccountGroupService(
    private val accountGroupRepository: AccountGroupRepository
) {
    @Transactional(readOnly = true)
    fun findAccountGroups(groupNames: Set<String>): List<AccountGroup> {
        return accountGroupRepository.findByNameIn(groupNames.toList())
    }
}
