package com.example.llm61.data.payment

import retrofit2.http.Body
import retrofit2.http.POST

interface PaymentApi {
    @POST("api/create-payment-intent")
    suspend fun createPaymentIntent(
        @Body request: CreatePaymentIntentRequest
    ): CreatePaymentIntentResponse

    @POST("api/restore-subscription")
    suspend fun restoreSubscription(
        @Body request: RestoreSubscriptionRequest
    ): RestoreSubscriptionResponse
}

data class CreatePaymentIntentRequest(
    val tier: String,
    val currentTier: String,
    val currentTierPurchasedAt: Long = 0,
    val auth0Sub: String = ""
)

data class CreatePaymentIntentResponse(
    val clientSecret: String,
    val amountCharged: Int = 0,
    val proratedCredit: Int = 0,
    val daysRemaining: Int = 0
)

data class RestoreSubscriptionRequest(val auth0Sub: String)
data class RestoreSubscriptionResponse(
    val tier: String = "free",
    val tierPurchasedAt: Long = 0,
    val paymentCount: Int = 0
)