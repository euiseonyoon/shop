package com.example.shop.auth.security.user_services

import com.example.shop.auth.models.CustomUserDetails
import com.example.shop.auth.services.AccountService
import com.example.shop.common.utils.AuthorityUtils
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

class EmailPasswordUserDetailService(
    private val accountService: AccountService,
) : UserDetailsService {
    // JdbcDaoImpl.loadUserByUsername를 대체한다
    override fun loadUserByUsername(username: String): UserDetails {
        // 1. find user by email
        val account = accountService.findWithAuthoritiesByEmail(username)
            ?: throw UsernameNotFoundException("Account Not found. username: $username")

        // 2. find user authorities ( role authority + group authorities)
        val accountAllAuthorities = AuthorityUtils.createSimpleGrantedAuthorities(account)

        // 3. create `CustomUserDetail`
        return CustomUserDetails(
            account.username!!,
            account.password!!,
            accountAllAuthorities,
            account.enabled,
            true,
            true,
            true,
            account.oauth,
            account.nickname
        )
    }
}
