package com.example.shop.auth.services

import com.example.shop.auth.domain.Authority
import com.example.shop.auth.exceptions.AuthorityNotFoundException
import com.example.shop.auth.models.NewAccountRequest
import com.example.shop.auth.repositories.AuthorityRepository
import com.example.shop.common.apis.exceptions.BadRequestException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthorityService(
    private val authorityRepository: AuthorityRepository,
) {
    @Transactional(readOnly = true)
    fun findByRoleName(name: String): Authority? = authorityRepository.findByRoleName(name)

    @Transactional
    fun createNewAuthority(name: String, hierarchy: Int): Authority = authorityRepository.save(Authority(name, hierarchy))

    @Transactional(readOnly = true)
    fun findAllByHierarchyAsc(): List<Authority> {
        return authorityRepository.findAllByOrderByHierarchyAsc()
    }

    @Transactional(readOnly = true)
    fun findWithPage(pageable: Pageable): Page<Authority> {
        return authorityRepository.findAll(pageable)
    }

    @Transactional
    fun createAuthority(name: String, hierarchy: Int): Authority {
        return authorityRepository.save(Authority(name, hierarchy))
    }

    @Transactional
    fun updateAuthorityHierarchy(accountId: Long, hierarchy: Int): Authority {
        val authority = authorityRepository.findById(accountId).orElse(null) ?:
            throw BadRequestException("Authority not found with id of $accountId")
        authority.hierarchy = hierarchy
        return authorityRepository.save(authority)
    }

    @Transactional
    fun getOrCreateAuthority(roleRequest: NewAccountRequest.RoleRequest): Authority {
        return findByRoleName(roleRequest.roleName.name) ?: run {
            if (!roleRequest.createIfNotExist) {
                throw AuthorityNotFoundException("${roleRequest.roleName} authority not found.")
            }
            createNewAuthority(roleRequest.roleName.name, roleRequest.roleHierarchy)
        }
    }
}
