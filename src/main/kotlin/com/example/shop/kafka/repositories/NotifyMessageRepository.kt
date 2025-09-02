package com.example.shop.kafka.repositories

import com.example.shop.kafka.domain.NotifyMessage
import org.springframework.data.jpa.repository.JpaRepository

interface NotifyMessageRepository: JpaRepository<NotifyMessage, Long> {}
