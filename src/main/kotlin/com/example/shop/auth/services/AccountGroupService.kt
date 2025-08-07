package com.example.shop.auth.services

import com.example.shop.admin.controllers.models.AccountGroupCreateRequest
import com.example.shop.admin.controllers.models.AccountGroupUpdateRequest
import com.example.shop.auth.domain.AccountGroup
import com.example.shop.auth.extension_functions.toAccountGroupDto
import com.example.shop.auth.repositories.AccountGroupRepository
import com.example.shop.common.apis.exceptions.BadRequestException
import com.example.shop.common.apis.models.AccountGroupDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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

    @Transactional(readOnly = true)
    fun findWithPage(pageable: Pageable): Page<AccountGroupDto> {
        return accountGroupRepository.findAll(pageable).map { it.toAccountGroupDto() }
    }

    @Transactional
    fun createAccountGroup(request: AccountGroupCreateRequest): AccountGroupDto {
        return accountGroupRepository.save(AccountGroup(request.name)).toAccountGroupDto()
    }

    @Transactional
    fun updateAccountGroup(request: AccountGroupUpdateRequest): AccountGroupDto {
        val accountGroup = accountGroupRepository.findById(request.id).orElse(null) ?:
            throw BadRequestException("Account Group not found with the id of ${request.id}")
        accountGroup.name = request.name
        return accountGroupRepository.save(accountGroup).toAccountGroupDto()
    }
}
