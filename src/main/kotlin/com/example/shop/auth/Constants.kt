package com.example.shop.auth

const val ROLE_PREFIX = "ROLE_"
const val USER_NAME = "USER"
const val ADMIN_NAME = "ADMIN"
const val ROLE_USER = ROLE_PREFIX + USER_NAME
const val ROLE_ADMIN = ROLE_PREFIX + ADMIN_NAME

val PERMIT_ALL_END_POINTS = listOf("/", "/health-check")

const val REFRESH_TOKEN_KEY = "refreshToken"
