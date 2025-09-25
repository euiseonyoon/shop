package com.example.shop.common.apis.exceptions

import com.example.shop.common.exceptions.CustomUncheckedException
import org.springframework.http.HttpStatus

class NotFoundException(
    msg: String
) : CustomUncheckedException(msg, HttpStatus.NOT_FOUND)
