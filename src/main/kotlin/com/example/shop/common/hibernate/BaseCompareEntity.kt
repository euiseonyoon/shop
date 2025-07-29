package com.example.shop.common.hibernate

import org.hibernate.Hibernate
import org.hibernate.proxy.HibernateProxy

abstract class BaseCompareEntity<EntityType> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false

        // Hibernate 프록시 클래스 비교
        if (Hibernate.getClass(this) != Hibernate.getClass(other)) return false

        return if (other !is HibernateProxy) {
            @Suppress("UNCHECKED_CAST")
            compareDetail(other as EntityType)
        } else {
            checkTargetIfProxy(other)
        }
    }

    private fun checkTargetIfProxy(other: HibernateProxy): Boolean {
        // target(엔티티)가 초기화 확인
        return if (Hibernate.isInitialized(other)) {
            @Suppress("UNCHECKED_CAST")
            val target = other.hibernateLazyInitializer.implementation as EntityType
            compareDetail(target)
        } else {
            compareByIdentifierWhenProxy(other)
        }
    }

    abstract fun compareDetail(other: EntityType): Boolean

    abstract fun compareByIdentifierWhenProxy(other: HibernateProxy): Boolean

    abstract fun hashCodeGenerator(): Int

    override fun hashCode(): Int {
        return hashCodeGenerator()
    }
}
