package com.example.shop.auth.domain

import com.example.shop.common.apis.models.AccountGroupDto
import com.example.shop.common.hibernate.BaseCompareEntity
import jakarta.persistence.CascadeType
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

    @OneToMany(mappedBy = "accountGroup", cascade = [CascadeType.ALL])
    val groupMemberMap: MutableSet<GroupMember> = mutableSetOf()

    @OneToMany(mappedBy = "accountGroup", cascade = [CascadeType.ALL])
    val authorities: MutableSet<GroupAuthority> = mutableSetOf()

    constructor()
    constructor(name: String) {
        this.name = name
    }

    fun addGroupAuthority(groupAuthority: GroupAuthority) {
        if (groupAuthority.accountGroup != this) {
            groupAuthority.accountGroup = this
        }
        this.authorities.add(groupAuthority)
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

    fun toDto(): AccountGroupDto {
        return AccountGroupDto(this.id!!, this.name!!)
    }
}
