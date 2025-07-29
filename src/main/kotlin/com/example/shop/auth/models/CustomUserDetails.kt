package com.example.shop.auth.models

import com.example.shop.auth.security.third_party.enums.ThirdPartyAuthenticationVendor
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User

class CustomUserDetails(
    username: String,
    passwordHash: String,
    authorities: Collection<GrantedAuthority>,
    enabled: Boolean,
    accountNonExpired: Boolean,
    credentialsNonExpired: Boolean,
    accountNonLocked: Boolean,
    val oauth: ThirdPartyAuthenticationVendor?,
    val nickname: String?,
) : User(username, passwordHash, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities) {

    override fun toString(): String {
        return "CustomUserDetails(username='$username')"
    }
}
