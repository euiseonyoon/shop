package com.example.shop.common.utils

import com.example.shop.auth.ROLE_PREFIX
import com.example.shop.common.utils.exceptions.AuthorityPrefixException

class AuthorityUtils {
    companion object {
        fun validateAuthorityPrefix(roleName: String) {
            if (!roleName.startsWith(ROLE_PREFIX)) {
                throw AuthorityPrefixException(roleName)
            }
        }
    }
}
