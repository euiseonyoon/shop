package com.example.shop.constants

const val ROLE_PREFIX = "ROLE_"
const val USER_NAME = "USER"
const val ADMIN_NAME = "ADMIN"
const val SUPER_ADMIN_NAME = "SUPER_ADMIN"

const val ROLE_USER = ROLE_PREFIX + USER_NAME
const val ROLE_ADMIN = ROLE_PREFIX + ADMIN_NAME
const val ROLE_SUPER_ADMIN = ROLE_PREFIX + SUPER_ADMIN_NAME

const val SUPER_ADMIN_HIERARCHY: Int = 1
const val ADMIN_HIERARCHY: Int = 2
const val DEFAULT_USER_HIERARCHY: Int = 100

const val REFRESH_TOKEN_KEY = "refreshToken"
val ACCESS_TOKEN_EXPIRATION_MS: Long = 1000 * 60 * 30 // 1시간
val REFRESH_TOKEN_EXPIRATION_MS: Long = 1000 * 60 * 60 * 24 * 7 // 7일

const val REDIS_AUTHORITY_REFRESH_CHANNEL = "authority-refresh-channel"
