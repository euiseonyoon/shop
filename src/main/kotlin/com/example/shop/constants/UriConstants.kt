package com.example.shop.constants

const val AUTH_URI_PREFIX = "/auth"
const val ADMIN_URI_PREFIX = "/admin"
const val ADMIN_AUTH_URI_PREFIX = "$ADMIN_URI_PREFIX/auth"
const val ADMIN_ACCOUNT_URI_PREFIX = "$ADMIN_URI_PREFIX/account"

const val HEALTH_CHECK_URI = "/health-check"
const val EMAIL_PASSWORD_AUTH_URI = "${AUTH_URI_PREFIX}/login"
const val OAUTH_AUTH_URI_PATTERN = "${AUTH_URI_PREFIX}/login/oauth/*"
const val TOKEN_REFRESH_URI = "${AUTH_URI_PREFIX}/token/refresh"

val PERMIT_ALL_END_POINTS = listOf("/", HEALTH_CHECK_URI, TOKEN_REFRESH_URI)
val NO_API_LIMIT_END_POINTS = listOf("/", HEALTH_CHECK_URI)
