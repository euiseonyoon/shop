package com.example.shop.auth.domain

import com.example.shop.common.apis.models.GroupAuthorityDto
import com.example.shop.common.hibernate.BaseCompareEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.proxy.HibernateProxy

@Entity
class GroupAuthority(
    @Column(nullable = false, unique = true)
    var name: String,

    @ManyToOne
    @JoinColumn(name = "account_group_id", nullable = false)
    var accountGroup: AccountGroup

) : BaseCompareEntity<GroupAuthority>() {
    @Id @GeneratedValue
    val id: Long = 0

    override fun compareDetail(other: GroupAuthority): Boolean = this.name == other.name

    override fun compareByIdentifierWhenProxy(other: HibernateProxy): Boolean {
        return (other.hibernateLazyInitializer.identifier as Long) == this.id
    }

    override fun hashCodeGenerator(): Int = name.hashCode()

    fun toDto(): GroupAuthorityDto {
        return GroupAuthorityDto(
            this.id,
            this.name,
            this.accountGroup.id,
            this.accountGroup.name
        )
    }
}
