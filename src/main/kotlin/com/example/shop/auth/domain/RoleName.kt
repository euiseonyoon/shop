package com.example.shop.auth.domain

import com.example.shop.common.utils.exceptions.AuthorityPrefixException
import com.example.shop.constants.ROLE_PREFIX

data class RoleName(
    val name: String
) {
    init {
        if (!name.startsWith(ROLE_PREFIX)) {
            throw AuthorityPrefixException(name)
        }
    }

    override fun toString(): String = name
}
