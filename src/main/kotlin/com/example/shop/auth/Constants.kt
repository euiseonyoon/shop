package com.example.shop.auth

const val ROLE_PREFIX = "ROLE_"
const val USER_NAME = "USER"
const val ADMIN_NAME = "ADMIN"
const val ROLE_USER = ROLE_PREFIX + USER_NAME
const val ROLE_ADMIN = ROLE_PREFIX + ADMIN_NAME

const val HEALTH_CHECK_URI = "/health-check"
const val EMAIL_PASSWORD_AUTH_URI = "/login"
const val OAUTH_AUTH_URI_PATTERN = "/login/oauth/*"
const val TOKEN_REFRESH_URI = "/token/refresh"

val PERMIT_ALL_END_POINTS = listOf("/", HEALTH_CHECK_URI, TOKEN_REFRESH_URI)

const val REFRESH_TOKEN_KEY = "refreshToken"
val ACCESS_TOKEN_EXPIRATION_MS: Long = 1000 * 60 * 30 // 1시간
val REFRESH_TOKEN_EXPIRATION_MS: Long = 1000 * 60 * 60 * 24 * 7 // 7일
