package com.example.shop.auth.security.user_services

import com.example.shop.auth.domain.Email
import com.example.shop.auth.models.CustomUserDetails
import com.example.shop.auth.services.AccountDomainService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

class EmailPasswordUserDetailService(
    private val accountDomainService: AccountDomainService,
) : UserDetailsService {
    // JdbcDaoImpl.loadUserByUsername를 대체한다
    override fun loadUserByUsername(username: String): UserDetails {
        // 1. find user by email
        val accountDomain = accountDomainService.findByEmail(Email(username))
            ?: throw UsernameNotFoundException("Account Not found. email: $username")

        // 2. create `CustomUserDetail`
        return CustomUserDetails(
            accountDomain.account,
            accountDomain.authorities,
        )
    }
}
