package com.example.shop.auth.domain

import com.example.shop.auth.ROLE_PREFIX
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToOne

@Entity
class Authority {
    @Id
    @GeneratedValue
    val id: Long? = null

    @OneToOne(mappedBy = "authority")
    var account: Account? = null

    @Column(nullable = false, unique = true)
    var roleName: String? = null

    constructor()
    constructor(roleName: String) {
        require(roleName.startsWith(ROLE_PREFIX)) {
            "Authority should start with $ROLE_PREFIX"
        }
        this.roleName = roleName
    }
}
