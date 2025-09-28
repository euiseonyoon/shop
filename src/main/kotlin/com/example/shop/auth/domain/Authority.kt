package com.example.shop.auth.domain

import com.example.shop.common.apis.models.AuthorityDto
import com.example.shop.constants.ROLE_PREFIX
import com.example.shop.common.hibernate.BaseCompareEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import org.hibernate.proxy.HibernateProxy

@Entity
class Authority(
    @Column(nullable = false, unique = true)
    var roleName: String,

    @Column(nullable = false, unique = true)
    var hierarchy: Int

) : BaseCompareEntity<Authority>() {
    init {
        require(roleName.startsWith(ROLE_PREFIX)) { "Authority should start with $ROLE_PREFIX" }
    }

    @Id @GeneratedValue
    val id: Long = 0

    override fun compareDetail(other: Authority): Boolean = other.roleName == this.roleName

    override fun compareByIdentifierWhenProxy(other: HibernateProxy): Boolean {
        return (other.hibernateLazyInitializer.identifier as Long) == this.id
    }

    override fun hashCodeGenerator(): Int = roleName.hashCode()

    fun toDto(): AuthorityDto {
        return AuthorityDto(
            this.id,
            this.roleName,
            this.hierarchy,
        )
    }
}
