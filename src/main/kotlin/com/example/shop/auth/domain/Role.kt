package com.example.shop.auth.domain

import com.example.shop.common.utils.exceptions.AuthorityPrefixException
import com.example.shop.constants.ROLE_PREFIX
import jakarta.persistence.Embeddable

@Embeddable
class Role(
    val name: String
) {
    init {
        if (!name.startsWith(ROLE_PREFIX)) {
            throw AuthorityPrefixException(name)
        }
    }

    override fun toString(): String = name

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is Role) return false
        return other.name == this.name
    }

    override fun hashCode(): Int = name.hashCode()
}
