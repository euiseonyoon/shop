package com.example.shop.configurations.swagger

import com.example.shop.constants.EMAIL_PASSWORD_AUTH_URI
import com.example.shop.constants.OAUTH_AUTH_URI_PATTERN
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    private val securitySchemeName = "bearerAuth"

    private fun makeComponents(): Components {
        return Components().apply {
            // JWT access token을 "Authorization" request 헤더에 넣을 수 있게 한다.
            val jwtScheme = getJwtAuthorizationSchema()
            this.addSecuritySchemes(jwtScheme.first, jwtScheme.second)

            // 로그인 관련 request, response를 보여줄 때 사용할 schema를 만들어 추가해준다.
            for ((key, schema) in getLogInSchemas()) {
                this.addSchemas(key, schema)
            }
        }
    }

    private fun getJwtAuthorizationSchema(): Pair<String, SecurityScheme> {
        return securitySchemeName to SecurityScheme()
            .name(securitySchemeName)
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
    }

    private fun getLogInSchemas(): Map<String, Schema<Any>> {
        return mapOf(
            "EmailLogInRequest" to Schema<Any>()
                .addProperty("email", Schema<Any>().type("string").description("이메일"))
                .addProperty("password", Schema<Any>().type("string").description("비밀번호")),

            "GoogleLogInRequest" to Schema<Any>()
                .addProperty("token", Schema<Any>().type("string").description("Google OIDC Id token")),

            "TokenResponse" to Schema<Any>()
                .addProperty("accessToken", Schema<Any>().type("string").description("접근에 사용되는 JWT 토큰")),

            // 로그인 성공 시 응답
            "LogInSuccessResponse" to Schema<Any>()
                .addProperty("isError", Schema<Any>().type("boolean").description("API 호출 성공 여부").example(false))
                .addProperty("result", Schema<Any>().`$ref`("#/components/schemas/TokenResponse"))
                .addProperty("errorMsg", Schema<Any>().type("string").nullable(true).description("성공 시에는 항상 null").example(null)),

            // 로그인 실패시 응답
            "LogInFailureResponse" to Schema<Any>()
                .addProperty("isError", Schema<Any>().type("boolean").description("API 호출 성공 여부").example(true))
                .addProperty("result", Schema<Any>().nullable(true).description("오류 시에는 항상 null").example(null))
                .addProperty("errorMsg", Schema<Any>().type("string").description("오류 메시지").example("이메일 또는 비밀번호가 잘못되었습니다."))

        )
    }

    private fun makeJsonContent(ref: String): Content {
        return Content()
            .addMediaType(
                "application/json",
                MediaType().schema(
                    Schema<Any>().`$ref`(ref)
                )
            )
    }

    private fun makeLogInResponses(): ApiResponses {
        return ApiResponses()
            .addApiResponse("200", ApiResponse().description("로그인 성공")
                .content(
                    makeJsonContent("#/components/schemas/LogInSuccessResponse")
                )
            )
            .addApiResponse("401", ApiResponse().description("인증 실패")
                .content(
                    makeJsonContent("#/components/schemas/LogInFailureResponse")
                )
            )
    }

    private fun makeLogInPathItem(summary: String, requestRef: String): PathItem {
        return PathItem().post(
            Operation()
                .summary(summary)
                .requestBody(
                    RequestBody().required(true).content(makeJsonContent(requestRef))
                )
                .responses(
                    makeLogInResponses()
                )
        )
    }

    private fun getLogInPathItems(): Map<String, PathItem> {
        return mapOf(
            EMAIL_PASSWORD_AUTH_URI to
                    makeLogInPathItem("이메일 + 비밀번호 로그인", "#/components/schemas/EmailLogInRequest"),
            OAUTH_AUTH_URI_PATTERN.replace("*", "google") to
                makeLogInPathItem("구글 OpenId 로그인", "#/components/schemas/GoogleLogInRequest"),
        )
    }

    private fun makeLogInPaths(): Paths {
        return Paths().apply {
            for ((key, pathItem) in getLogInPathItems()) {
                this.addPathItem(key, pathItem)
            }
        }
    }

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(Info().title("Shop API").version("v1.0"))
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
            .components(makeComponents())
            .paths(makeLogInPaths())
    }
}