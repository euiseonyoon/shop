package com.example.shop.auth.domain

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

data class AccountDomain(
    val account: Account,
    val accountGroupMap: MutableMap<AccountGroup, List<GroupAuthority>>,
) {
    val authority: Authority = account.authority

    val authorities: Set<GrantedAuthority>
        get() {
            val singleAuthority = SimpleGrantedAuthority(authority.roleName)
            val groupAuthorities = accountGroupMap.values.flatten().map { SimpleGrantedAuthority(it.name) }
            return (groupAuthorities + singleAuthority).toSet()
        }
}
