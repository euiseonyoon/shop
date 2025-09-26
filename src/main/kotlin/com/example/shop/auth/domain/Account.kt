package com.example.shop.auth.domain

import com.example.shop.auth.security.third_party.enums.ThirdPartyAuthenticationVendor
import com.example.shop.common.hibernate.BaseCompareEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import org.hibernate.annotations.NaturalId
import org.hibernate.proxy.HibernateProxy


@Entity
class Account(
    @Column(nullable = false, unique = true) @NaturalId
    val email: String,

    @Column(nullable = false)
    var passwordHash: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authority_id", nullable = false)
    var authority: Authority,

    @Column(nullable = false)
    var enabled: Boolean = true,

    @Column(nullable = true)
    var nickname: String? = null,

    @Column(nullable = true) @Enumerated(value = EnumType.STRING)
    var oauth: ThirdPartyAuthenticationVendor? = null,
): BaseCompareEntity<Account>() {
    @Id @GeneratedValue
    val id: Long = 0

    @OneToMany(mappedBy = "account", cascade = [CascadeType.ALL])
    val _groupMemberMap: MutableSet<GroupMember> = mutableSetOf()

    fun addGroupMember(groupMember: GroupMember): Set<GroupMember> {
        this._groupMemberMap.add(groupMember)
        return _groupMemberMap
    }

    fun removeGroupMember(groupMember: GroupMember): Set<GroupMember> {
        this._groupMemberMap.remove(groupMember)
        return _groupMemberMap
    }

    val groupMembers: Set<GroupMember>
        get() = _groupMemberMap

    val accountGroups: List<AccountGroup>
        get() = _groupMemberMap.mapNotNull { it.accountGroup }

    val groupAuthorities: List<GroupAuthority>
        get() = accountGroups.flatMap { it.authorities }

    fun addRole(role: Authority) {
        if (this.authority != role) {
            this.authority = role
        }
        role.accounts.add(this)
    }

    override fun compareDetail(other: Account): Boolean {
        if (email != other.email) return false
        return true
    }

    override fun compareByIdentifierWhenProxy(other: HibernateProxy): Boolean {
        return (other.hibernateLazyInitializer.identifier as Long) == this.id
    }

    override fun hashCodeGenerator(): Int = email.hashCode()
}
