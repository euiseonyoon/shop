package com.example.shop.auth.domain

import com.example.shop.common.hibernate.BaseCompareEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.proxy.HibernateProxy

@Entity
class GroupAuthority: BaseCompareEntity<GroupAuthority> {
    @Id
    @GeneratedValue
    val id: Long? = null

    @Column(nullable = false, unique = true)
    var name: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_group_id")
    var accountGroup: AccountGroup? = null

    constructor()
    constructor(name: String) {
        this.name = name
    }

    override fun compareDetail(other: GroupAuthority): Boolean {
        return this.name == other.name
    }

    override fun compareByIdentifierWhenProxy(other: HibernateProxy): Boolean {
        return (other.hibernateLazyInitializer.identifier as Long) == this.id
    }

    override fun hashCodeGenerator(): Int {
        return name?.hashCode() ?: 0
    }
}
