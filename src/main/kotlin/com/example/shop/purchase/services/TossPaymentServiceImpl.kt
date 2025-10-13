package com.example.shop.purchase.services

import com.example.shop.purchase.exceptions.TossPaymentApiException
import com.example.shop.purchase.models.PurchaseApproveRequest
import com.example.shop.purchase.models.pg_payment_result.toss.TossErrorResponse
import com.example.shop.purchase.models.pg_payment_result.toss.TossPaymentResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.Base64

@Service
class TossPaymentServiceImpl(
    @Value("\${payment.toss.secret}")
    secret: String,
) : TossPaymentService {
    override val baseUrl = "https://api.tosspayments.com"
    override val paymentApprovePath = "/v1/payments/confirm"
    override val webClient = WebClient.create(baseUrl)

    override val secretKey = secret
    private val authHeader = "Basic " + Base64.getEncoder().encodeToString("$secretKey:".toByteArray())

    override fun sendPaymentApproveRequest(request: PurchaseApproveRequest): TossPaymentResponse? {
        /**
         *
         * curl --request POST \
         *   --url https://api.tosspayments.com/v1/payments/confirm \
         *   --header 'Authorization: Basic dGVzdF9za196WExrS0V5cE5BcldtbzUwblgzbG1lYXhZRzVSOg==' \
         *   --header 'Content-Type: application/json' \
         *   --data '{"paymentKey":"5EnNZRJGvaBX7zk2yd8ydw26XvwXkLrx9POLqKQjmAw4b0e1","orderId":"a4CWyWY5m89PNh7xJwhk1","amount":1000}'
         * */
        return webClient.post()
            .uri { it.path(paymentApprovePath).build() }
            .header("Authorization", authHeader)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .onStatus({ status -> status.isError }) { clientResponse ->
                handleBadResponse(clientResponse)
            }
            .bodyToMono(TossPaymentResponse::class.java)
            .block()
    }

    private fun handleBadResponse(clientResponse: ClientResponse): Mono<TossPaymentApiException> {
        return clientResponse
            .bodyToMono(TossErrorResponse::class.java)
            .flatMap { errorBody ->
                Mono.error(
                    TossPaymentApiException(
                        errorCode = errorBody.code,
                        errorMessage = errorBody.message,
                        httpStatus = HttpStatus.valueOf(clientResponse.statusCode().value()),
                    )
                )
            }
    }
}

