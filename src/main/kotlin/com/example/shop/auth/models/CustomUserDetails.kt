package com.example.shop.auth.models

import com.example.shop.auth.domain.Account
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User

class CustomUserDetails(
    val account: Account,
    val allAuthorities: Collection<GrantedAuthority>,
    accountNonExpired: Boolean = true,
    credentialsNonExpired: Boolean = true,
    accountNonLocked: Boolean = true,
) : User(account.email!!, account.password!!, account.enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, allAuthorities) {

    init {
        requireNotNull(account.email) { "Custom user detail exception. Account.username should not be null." }
        requireNotNull(account.id) { "Custom user detail exception. Account.id should not be null." }
    }

    override fun toString(): String {
        return "CustomUserDetails(username='$username')"
    }
}
