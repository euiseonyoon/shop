package com.example.shop.auth.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToMany

@Entity
class Group {
    @Id
    @GeneratedValue
    val id: Long? = null

    @Column(nullable = false)
    var name: String? = null

    @OneToMany(mappedBy = "group")
    val groupMemberMap: MutableSet<GroupMember> = mutableSetOf()

    @OneToMany(mappedBy = "group")
    val authorities: MutableSet<GroupAuthority> = mutableSetOf()
}
