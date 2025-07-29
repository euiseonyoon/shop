package com.example.shop.common.utils.exceptions

import com.example.shop.common.exceptions.CustomUncheckedException

class AuthorityPrefixException(
    givenRoleName: String
) : CustomUncheckedException("Inadequate authority name. given : $givenRoleName", null)
