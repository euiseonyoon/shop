package com.example.shop.purchase.repositories

import com.example.shop.purchase.domain.Purchase
import com.example.shop.purchase.repositories.extensions.PurchaseRepositoryExtension
import org.springframework.data.jpa.repository.JpaRepository

interface PurchaseRepository: JpaRepository<Purchase, Long>, PurchaseRepositoryExtension {

}
