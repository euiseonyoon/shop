package com.example.shop.security.db.entities

import com.example.shop.security.third_party_auth.enums.ThirdPartyAuthenticationVendor
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Transient


@Entity
class Account {
    @Id
    @GeneratedValue
    val id: Long? = null

    @Column(nullable = false)
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
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "authority_id", unique = true, nullable = false)
    var authority: Authority? = null

    @OneToMany(mappedBy = "account")
    val groupMemberMap: MutableSet<GroupMember> = mutableSetOf()

    @Transient
    val group: List<Group> = groupMemberMap.mapNotNull { it.group }

    @Transient
    val groupAuthorities: List<GroupAuthority> = this.group.flatMap { it.authorities }

    fun addRole(role: Authority) {
        if (this.authority != role) {
            this.authority = role
        }
        role.account = this
    }
}
