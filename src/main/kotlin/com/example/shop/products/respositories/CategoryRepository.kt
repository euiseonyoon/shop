package com.example.shop.products.respositories

import com.example.shop.products.domain.Category
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository : JpaRepository<Category, Long>{
}
