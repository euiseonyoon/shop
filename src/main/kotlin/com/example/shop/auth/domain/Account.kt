package com.example.shop.auth.domain

import com.example.shop.auth.security.third_party.enums.ThirdPartyAuthenticationVendor
import com.example.shop.common.hibernate.BaseCompareEntity
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
import org.hibernate.proxy.HibernateProxy


@Entity
class Account: BaseCompareEntity<Account>() {
    @Id
    @GeneratedValue
    val id: Long? = null

    @Column(nullable = false, unique = true)
    var username: String? = null

    @Column(nullable = false)
    var password: String? = null

    @Column(nullable = false)
    var enabled: Boolean = true

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = true)
    var oauth: ThirdPartyAuthenticationVendor? = null

    @Column(nullable = true)
    var nickname: String? = null

    // 유저당 ROLE을 1개만 갖도록 강제한다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authority_id", nullable = false)
    var authority: Authority? = null

    @OneToMany(mappedBy = "account")
    val groupMemberMap: MutableSet<GroupMember> = mutableSetOf()

    fun getGroups(): List<AccountGroup> {
        return groupMemberMap.mapNotNull { it.accountGroup }
    }

    fun getGroupAuthorities(): List<GroupAuthority> {
        return getGroups().flatMap { it.authorities }
    }

    fun addRole(role: Authority) {
        if (this.authority != role) {
            this.authority = role
        }
        role.accounts.add(this)
    }

    override fun compareDetail(other: Account): Boolean {
        if (username != other.username) return false
        return true
    }

    override fun compareByIdentifierWhenProxy(other: HibernateProxy): Boolean {
        return (other.hibernateLazyInitializer.identifier as Long) == this.id
    }

    override fun hashCodeGenerator(): Int {
        return username?.hashCode() ?: 0
    }
}
