package com.example.shop.common.utils.exceptions

import com.example.shop.common.exceptions.CustomUncheckedException

class InvalidEmailAddressException(
    givenEmailAddress: String
) : CustomUncheckedException("Invalid email address : $givenEmailAddress", null)
