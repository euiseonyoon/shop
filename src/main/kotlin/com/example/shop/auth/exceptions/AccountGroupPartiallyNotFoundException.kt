package com.example.shop.auth.exceptions

import com.example.shop.common.exceptions.CustomUncheckedException
import org.springframework.http.HttpStatus

class AccountGroupPartiallyNotFoundException(msg: String): CustomUncheckedException(msg, HttpStatus.NOT_FOUND)