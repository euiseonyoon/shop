package com.example.shop.auth.services

import com.example.shop.admin.controllers.models.AuthorityCreateRequest
import com.example.shop.admin.controllers.models.AuthorityUpdateRequest
import com.example.shop.common.apis.models.AuthorityDto
import com.example.shop.auth.domain.Authority
import com.example.shop.auth.extension_functions.toAuthorityDto
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
    fun findWithPage(pageable: Pageable): Page<AuthorityDto> {
        return authorityRepository.findAll(pageable).map { it.toAuthorityDto() }
    }

    @Transactional
    fun createAuthority(request: AuthorityCreateRequest): AuthorityDto {
        val authority = Authority(request.name, request.hierarchy)
        return authorityRepository.save(authority).toAuthorityDto()
    }

    @Transactional
    fun updateAuthorityHierarchy(request: AuthorityUpdateRequest): AuthorityDto {
        val authority = authorityRepository.findById(request.id).orElse(null) ?:
            throw BadRequestException("Authority not found with id of ${request.id}")
        authority.hierarchy = request.hierarchy
        return authorityRepository.save(authority).toAuthorityDto()
    }
}
