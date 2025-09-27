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
class AccountGroup(
    @Column(nullable = false, unique = true)
    var name: String

) : BaseCompareEntity<AccountGroup>() {
    @Id @GeneratedValue
    val id: Long = 0

    override fun compareDetail(other: AccountGroup): Boolean = name == other.name

    override fun compareByIdentifierWhenProxy(other: HibernateProxy): Boolean {
        return (other.hibernateLazyInitializer.identifier as Long) == this.id
    }

    override fun hashCodeGenerator(): Int {
        return name.hashCode()
    }

    fun toDto(): AccountGroupDto {
        return AccountGroupDto(this.id, this.name)
    }
}
