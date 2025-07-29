package com.example.shop.auth.services

import com.example.shop.auth.domain.Authority
import com.example.shop.auth.repositories.AuthorityRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthorityService(
    private val authorityRepository: AuthorityRepository
) {
    @Transactional(readOnly = true)
    fun findByRoleName(name: String): Authority? = authorityRepository.findByRoleName(name)
}
