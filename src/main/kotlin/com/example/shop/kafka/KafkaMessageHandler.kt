package com.example.shop.kafka

interface KafkaMessageHandler<TMessage> {
    fun handleMessage(message: TMessage)
}
