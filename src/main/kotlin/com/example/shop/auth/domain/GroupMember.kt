package com.example.shop.auth.domain

import com.example.shop.common.hibernate.BaseCompareEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.proxy.HibernateProxy

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["account_id", "account_group_id"])
    ]
)
class GroupMember: BaseCompareEntity<GroupMember> {
    @Id
    @GeneratedValue
    val id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_group_id")
    var accountGroup: AccountGroup? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    var account: Account? = null

    constructor()
    constructor(account: Account, accountGroup: AccountGroup) {
        if (this.account != account) {
            this.account = account
        }
        if (this.accountGroup != accountGroup) {
            this.accountGroup = accountGroup
        }
        account.groupMemberMap.add(this)
        accountGroup.groupMemberMap.add(this)
    }

    override fun compareDetail(other: GroupMember): Boolean {
        if (accountGroup != other.accountGroup) return false
        if (account != other.account) return false
        return true
    }

    override fun compareByIdentifierWhenProxy(other: HibernateProxy): Boolean {
        return (other.hibernateLazyInitializer.identifier as Long) == this.id
    }

    override fun hashCodeGenerator(): Int {
        var result = accountGroup?.hashCode() ?: 0
        result = 31 * result + (account?.hashCode() ?: 0)
        return result
    }
}
