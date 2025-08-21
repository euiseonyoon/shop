package com.example.shop.products.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import org.hibernate.annotations.ColumnDefault


@Entity
@Table(
    indexes = [
        Index(name = "idx_category_name", columnList = "name"),
        Index(name = "idx_category_parent", columnList = "parent_id"),
        Index(name = "idx_category_enable", columnList = "is_enabled"),
        Index(name = "idx_category_full_path", columnList = "full_path"),
    ]
)
class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_seq_gen")
    @SequenceGenerator(
        name = "category_seq_gen",
        sequenceName = "category_id_seq",
        allocationSize = 1
    )
    val id: Long? = null

    @Column(nullable = false, unique = true)
    var name: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: Category? = null

    @Column(nullable = false)
    var fullPath: String? = null

    @Column(nullable = false)
    var isLast: Boolean = true

    @Column(nullable = false)
    @ColumnDefault("true")
    var isEnabled: Boolean = true
}
