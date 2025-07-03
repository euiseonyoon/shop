package com.example.shop.common.logger

import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class LogSupport {
    protected val logger: Logger = LoggerFactory.getLogger(this::class.java)
}
