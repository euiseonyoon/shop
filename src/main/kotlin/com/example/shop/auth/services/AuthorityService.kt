package com.example.shop.auth.services

import com.example.shop.auth.domain.Authority
import com.example.shop.auth.domain.Role
import com.example.shop.auth.exceptions.AuthorityNotFoundException
import com.example.shop.auth.models.AuthRequest
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
    fun findByRole(role: Role): Authority? = authorityRepository.findByRoleName(role.name)

    @Transactional
    fun createNewAuthority(roleRequest: AuthRequest.RoleRequest): Authority {
        require(roleRequest.createIfNotExist)

        return authorityRepository.save(Authority(roleRequest.role, roleRequest.roleHierarchy))
    }

    @Transactional(readOnly = true)
    fun findAllByHierarchyAscOrdered(): List<Authority> {
        return authorityRepository.findAllByOrderByHierarchyAsc()
    }

    @Transactional(readOnly = true)
    fun findWithPage(pageable: Pageable): Page<Authority> {
        return authorityRepository.findAll(pageable)
    }

    @Transactional
    fun updateAuthorityHierarchy(authorityId: Long, hierarchy: Int): Authority {
        val authority = authorityRepository.findById(authorityId).orElse(null) ?:
            throw BadRequestException("Authority not found with id of $authorityId")
        authority.hierarchy = hierarchy
        return authorityRepository.save(authority)
    }

    @Transactional
    fun getOrCreateAuthority(roleRequest: AuthRequest.RoleRequest): Authority {
        return findByRole(roleRequest.role) ?: run {
            if (!roleRequest.createIfNotExist) {
                throw AuthorityNotFoundException("${roleRequest.role} authority not found.")
            }
            createNewAuthority(roleRequest)
        }
    }
}
