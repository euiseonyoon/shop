package com.example.shop.auth.exceptions

import com.example.shop.common.exceptions.CustomUncheckedException
import org.springframework.http.HttpStatus

class FailedToRetrieveRefreshTokenException(
    msg: String
) : CustomUncheckedException(msg, HttpStatus.INTERNAL_SERVER_ERROR)
