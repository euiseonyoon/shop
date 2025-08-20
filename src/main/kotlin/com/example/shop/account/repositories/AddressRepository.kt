package com.example.shop.account.repositories

import com.example.shop.account.domain.Address
import org.springframework.data.jpa.repository.JpaRepository

interface AddressRepository : JpaRepository<Address, Long> {

}
