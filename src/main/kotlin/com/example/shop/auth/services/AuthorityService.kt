package com.example.shop.auth.services

import com.example.shop.auth.domain.Authority
import com.example.shop.auth.domain.RoleName
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
    fun findByRoleName(roleName: RoleName): Authority? = authorityRepository.findByRoleName(roleName.name)

    @Transactional
    fun createNewAuthority(roleRequest: AuthRequest.RoleRequest): Authority {
        require(roleRequest.createIfNotExist)

        return authorityRepository.save(Authority(roleRequest.roleName.name, roleRequest.roleHierarchy))
    }

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
    fun updateAuthorityHierarchy(authorityId: Long, hierarchy: Int): Authority {
        val authority = authorityRepository.findById(authorityId).orElse(null) ?:
            throw BadRequestException("Authority not found with id of $authorityId")
        authority.hierarchy = hierarchy
        return authorityRepository.save(authority)
    }

    @Transactional
    fun getOrCreateAuthority(roleRequest: AuthRequest.RoleRequest): Authority {
        return findByRoleName(roleRequest.roleName) ?: run {
            if (!roleRequest.createIfNotExist) {
                throw AuthorityNotFoundException("${roleRequest.roleName} authority not found.")
            }
            createNewAuthority(roleRequest)
        }
    }
}
