package com.example.shop.purchase.repositories

import com.example.shop.purchase.domain.Payment
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentRepository: JpaRepository<Payment, Long> {

}
