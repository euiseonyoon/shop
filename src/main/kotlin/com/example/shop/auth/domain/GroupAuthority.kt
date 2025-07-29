package com.example.shop.auth.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class GroupAuthority {
    @Id
    @GeneratedValue
    val id: Long? = null

    @Column(nullable = false)
    val name: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_group_id")
    var accountGroup: AccountGroup? = null
}
