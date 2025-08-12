package com.example.shop.redis.authority_refresh

interface AuthorityRefreshEventPublisher {
    fun publishAuthorityRefreshEvent(message: String)

    fun fallBackIfFailedToPublishEvent(message: String, e: Throwable)
}
