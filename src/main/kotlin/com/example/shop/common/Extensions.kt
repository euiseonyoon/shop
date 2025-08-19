package com.example.shop.common

fun Int.requireNonNegative(exception: Exception?): Int {
    try {
        require(this >= 0)
    } catch (e: Exception) {
        if (exception != null) {
            throw exception
        } else {
            throw e
        }
    }
    return this
}
