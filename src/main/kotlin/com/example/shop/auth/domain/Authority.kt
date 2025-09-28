package com.example.shop.auth.domain

import com.example.shop.common.apis.models.AuthorityDto
import com.example.shop.constants.ROLE_PREFIX
import com.example.shop.common.hibernate.BaseCompareEntity
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import org.hibernate.proxy.HibernateProxy

@Entity
class Authority(
    @Column(nullable = false, unique = true) @Embedded
    var role: Role,

    @Column(nullable = false, unique = true)
    var hierarchy: Int

) : BaseCompareEntity<Authority>() {

    @Id @GeneratedValue
    val id: Long = 0

    override fun compareDetail(other: Authority): Boolean = other.role == this.role

    override fun compareByIdentifierWhenProxy(other: HibernateProxy): Boolean {
        return (other.hibernateLazyInitializer.identifier as Long) == this.id
    }

    override fun hashCodeGenerator(): Int = role.hashCode()

    fun toDto(): AuthorityDto {
        return AuthorityDto(
            this.id,
            this.role.name,
            this.hierarchy,
        )
    }
}
