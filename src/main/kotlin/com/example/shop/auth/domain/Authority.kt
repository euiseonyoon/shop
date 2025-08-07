package com.example.shop.auth.domain

import com.example.shop.constants.ROLE_PREFIX
import com.example.shop.common.hibernate.BaseCompareEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import org.hibernate.proxy.HibernateProxy

@Entity
class Authority : BaseCompareEntity<Authority> {
    @Id
    @GeneratedValue
    val id: Long? = null

    @OneToMany(mappedBy = "authority")
    val accounts: MutableSet<Account> = mutableSetOf()

    @Column(nullable = false, unique = true)
    var roleName: String? = null

    constructor()
    constructor(roleName: String) {
        require(roleName.startsWith(ROLE_PREFIX)) {
            "Authority should start with $ROLE_PREFIX"
        }
        this.roleName = roleName
    }

    override fun compareDetail(other: Authority): Boolean {
        return other.roleName == this.roleName
    }

    override fun compareByIdentifierWhenProxy(other: HibernateProxy): Boolean {
        return (other.hibernateLazyInitializer.identifier as Long) == this.id
    }

    override fun hashCodeGenerator(): Int {
        return roleName?.hashCode() ?: 0
    }
}
