package com.example.shop.configurations

import kotlinx.serialization.json.Json
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.http.converter.json.KotlinSerializationJsonHttpMessageConverter


@Configuration
class WebConfig : WebMvcConfigurer {

    @Bean
    fun json(): Json = Json { ignoreUnknownKeys = true }

    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        // ObjectMapper 대신 kotlinx.serialization converter를 추가
        converters.add(0, KotlinSerializationJsonHttpMessageConverter(json()))
    }
}
