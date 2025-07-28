package com.example.shop.security.db.entities

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.Hibernate
import org.hibernate.proxy.HibernateProxy

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["group_id", "account_id"])
    ]
)
class GroupMember {
    @Id
    @GeneratedValue
    val id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    var group: Group? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    var account: Account? = null

    constructor()
    constructor(account: Account, group: Group) {
        if (this.account != account) {
            this.account = account
        }
        if (this.group != group) {
            this.group = group
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true

        if (Hibernate.getClass(this) != Hibernate.getClass(other)) return false

        return if (other !is HibernateProxy) {
            compareDetail(other as GroupMember)
        } else {
            checkTargetIfProxy(other)
        }
    }

    private fun checkTargetIfProxy(other: HibernateProxy): Boolean {
        // target(엔티티)가 초기화 확인
        return if (Hibernate.isInitialized(other)) {
            val target = other.hibernateLazyInitializer.implementation as GroupMember
            compareDetail(target)
        } else {
            val targetId = other.hibernateLazyInitializer.identifier as Long
            targetId == this.id
        }
    }

    private fun compareDetail(other: GroupMember): Boolean {
        if (group != other.group) return false
        if (account != other.account) return false

        return true
    }

    override fun hashCode(): Int {
        var result = group?.hashCode() ?: 0
        result = 31 * result + (account?.hashCode() ?: 0)
        return result
    }
}
