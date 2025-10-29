package com.example.shop.constants

const val AUTH_URI_PREFIX = "/auth"
const val ADMIN_URI_PREFIX = "/admin"
const val ADMIN_AUTH_URI_PREFIX = "$ADMIN_URI_PREFIX/auth"
const val ADMIN_ACCOUNT_URI_PREFIX = "$ADMIN_URI_PREFIX/account"

const val HEALTH_CHECK_URI = "/health-check"
const val LOGIN_URI = "${AUTH_URI_PREFIX}/login"
val LOGIN_URIS = listOf(
    "${LOGIN_URI}/email",
    "${LOGIN_URI}/oauth",
)
const val TOKEN_REFRESH_URI = "${AUTH_URI_PREFIX}/token/refresh"

val SWAGGER_END_POINTS = listOf(
    "/v3/api-docs/**",
    "/swagger-ui",
    "/swagger-ui/**",
    "/swagger-ui.html",
    "/swagger-resources/**",
    "/webjars/**",
    "/favicon**",
)

val PERMIT_ALL_END_POINTS = listOf("/", HEALTH_CHECK_URI, TOKEN_REFRESH_URI) + SWAGGER_END_POINTS + LOGIN_URIS
val NO_API_LIMIT_END_POINTS = listOf("/", HEALTH_CHECK_URI) + SWAGGER_END_POINTS
