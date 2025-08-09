package com.example.shop.auth.services

import com.example.shop.admin.models.auth.GroupAuthorityCreateRequest
import com.example.shop.admin.models.auth.GroupAuthorityDeleteRequest
import com.example.shop.admin.models.auth.GroupAuthorityUpdateRequest
import com.example.shop.auth.domain.AccountGroup
import com.example.shop.auth.domain.GroupAuthority
import com.example.shop.auth.repositories.AccountGroupRepository
import com.example.shop.auth.repositories.GroupAuthorityRepository
import com.example.shop.common.apis.exceptions.BadRequestException
import com.example.shop.common.apis.models.GroupAuthorityDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GroupAuthorityService(
    private val groupAuthorityRepository: GroupAuthorityRepository,
    private val accountGroupRepository: AccountGroupRepository,
) {
    @Transactional(readOnly = true)
    fun findWithPage(pageable: Pageable): Page<GroupAuthority> {
        return groupAuthorityRepository.findAll(pageable)
    }

    @Transactional
    fun createGroupAuthority(request: GroupAuthorityCreateRequest): GroupAuthority {
        val groupName = request.groupName
        val groupAuthorityName = request.name
        val group = accountGroupRepository.findByNameIn(listOf(groupName)).firstOrNull().let {
            if (it == null) {
                // 그룹 생성
                accountGroupRepository.save(AccountGroup(groupName))
            } else {
                it
            }
        }
        val groupAuthority = GroupAuthority(groupAuthorityName)
        groupAuthority.accountGroup = group

        return groupAuthorityRepository.save(groupAuthority)
    }

    @Transactional
    fun updateGroupAuthority(request: GroupAuthorityUpdateRequest): GroupAuthority {
        val groupAuthority = groupAuthorityRepository.findById(request.id).orElse(null) ?:
            throw BadRequestException("Group Authority not found with the id of ${request.id}")

        request.name?.let { groupAuthority.name = it }
        request.groupName?.let {
            accountGroupRepository.findByNameIn(listOf(it)).firstOrNull()?.let { accountGroup ->
                groupAuthority.accountGroup = accountGroup
            }
        }
        return groupAuthorityRepository.save(groupAuthority)
    }

    @Transactional
    fun deleteGroupAuthorities(request: GroupAuthorityDeleteRequest) {
        groupAuthorityRepository.deleteAllById(request.ids)
    }
}
