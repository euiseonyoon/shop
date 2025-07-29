package com.example.shop.auth.domain

import com.example.shop.common.hibernate.BaseCompareEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import org.hibernate.proxy.HibernateProxy

@Entity
class AccountGroup: BaseCompareEntity<AccountGroup> {
    @Id
    @GeneratedValue
    val id: Long? = null

    @Column(nullable = false, unique = true)
    var name: String? = null

    @OneToMany(mappedBy = "accountGroup")
    val groupMemberMap: MutableSet<GroupMember> = mutableSetOf()

    @OneToMany(mappedBy = "accountGroup")
    val authorities: MutableSet<GroupAuthority> = mutableSetOf()

    constructor()
    constructor(name: String) {
        this.name = name
    }

    override fun compareDetail(other: AccountGroup): Boolean {
        if (name != other.name) return false
        return true
    }

    override fun compareByIdentifierWhenProxy(other: HibernateProxy): Boolean {
        return (other.hibernateLazyInitializer.identifier as Long) == this.id
    }

    override fun hashCodeGenerator(): Int {
        return name?.hashCode() ?: 0
    }
}
