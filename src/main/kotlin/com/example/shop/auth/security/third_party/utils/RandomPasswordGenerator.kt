package com.example.shop.auth.security.third_party.utils


import org.springframework.stereotype.Component
import java.security.SecureRandom

@Component
class RandomPasswordGenerator : PasswordGenerator {
    private val CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz"
    private val CHAR_UPPER = CHAR_LOWER.uppercase()
    private val DIGIT = "0123456789"
    private val SPECIAL_CHARS = "!@#?"

    private val PASSWORD_CHARS = CHAR_LOWER + CHAR_UPPER + DIGIT + SPECIAL_CHARS
    private val RANDOM = SecureRandom() // 보안에 강력한 랜덤 생성기 사용

    val defaultPasswordLength = 12

    override fun generatePassword(length: Int?): String {
        val passwordLength = length ?: defaultPasswordLength

        // 최소한 각 문자 유형에서 하나씩 포함되도록 보장
        val passwordBuilder = StringBuilder(passwordLength)
        passwordBuilder.append(getRandomChar(CHAR_LOWER)) // 소문자
        passwordBuilder.append(getRandomChar(CHAR_UPPER)) // 대문자
        passwordBuilder.append(getRandomChar(DIGIT)) // 숫자
        passwordBuilder.append(getRandomChar(SPECIAL_CHARS)) // 특수 문자

        // 나머지 길이를 전체 문자 세트에서 랜덤하게 채움
        for (i in 4 .. passwordLength) {
            passwordBuilder.append(getRandomChar(PASSWORD_CHARS))
        }

        // 생성된 비밀번호 문자열을 섞어 무작위성을 높임
        return passwordBuilder.toString().toCharArray().apply { shuffle() }.joinToString("")
    }

    private fun getRandomChar(characterSet: String): Char {
        val randomIndex = RANDOM.nextInt(characterSet.length)
        return characterSet[randomIndex]
    }


}
