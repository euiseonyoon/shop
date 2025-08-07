package com.example.shop.common.apis.exceptions

import com.example.shop.common.exceptions.CustomUncheckedException
import org.springframework.http.HttpStatus

class BadRequestException(
    msg: String
) : CustomUncheckedException(msg, HttpStatus.BAD_REQUEST)
