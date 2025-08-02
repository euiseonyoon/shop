package com.example.shop.auth.utils

import com.example.shop.auth.exceptions.BadRefreshTokenStateException
import com.example.shop.common.logger.LogSupport
import com.example.shop.redis.tokens.repositories.RefreshTokenRedisRepository
import org.springframework.stereotype.Component

@Component
class RefreshTokenStateHelperImpl(
    private val refreshTokenRedisRepository: RefreshTokenRedisRepository
) : LogSupport(),
    RefreshTokenStateHelper {
    override fun validateRefreshToken(email: String, refreshTokenFromRequest: String) {
        val issuedRefreshToken = refreshTokenRedisRepository.find(email)

        /**
         * Redis에 해당 account의 refresh token 이 없는경우
         *
         * 1. 토큰을 처음부터 발급하지 않았거나.
         * 2. 이미 로그아웃 처리되거나.
         * 3. 혹은 expire가 되었거나, 하지만 이 경우는 `refresh token`에서 email을 추출하는 과정에서 이미 걸러졌을 것이다.
         * */
        if (issuedRefreshToken == null) {
            throw BadRefreshTokenStateException("Refresh token not found for account, but provided in request.")
        }

        // 전에 발급된 refresh token과 request에 포함된 refresh token이 다를 경우, 탈취된 refresh token일 수 있다.
        if (issuedRefreshToken != refreshTokenFromRequest) {
            logger.warn(
                "Refresh token not matching. email={}, IssuedRefreshToken={}, RefreshTokenFromRequest={}",
                email, issuedRefreshToken, refreshTokenFromRequest
            )
            throw BadRefreshTokenStateException(
                "Refresh token from the request did not match with the previously issued one for the account."
            )
        }
    }

    override fun updateWithNewRefreshToken(email: String, newRefreshToken: String) {
        refreshTokenRedisRepository.save(email, newRefreshToken)
    }
}
