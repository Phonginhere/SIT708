package com.example.llm61.data.payment

import com.example.llm61.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PaymentRepository {

    private val api: PaymentApi by lazy {
        val baseUrl = BuildConfig.VERCEL_BACKEND_URL.let {
            if (it.endsWith("/")) it else "$it/"
        }
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PaymentApi::class.java)
    }

    suspend fun createPaymentIntent(
        tier: String,
        currentTier: String,
        currentTierPurchasedAt: Long,
        auth0Sub: String
    ): Result<String> = runCatching {
        api.createPaymentIntent(
            CreatePaymentIntentRequest(tier, currentTier, currentTierPurchasedAt, auth0Sub)
        ).clientSecret
    }

    suspend fun restoreSubscription(auth0Sub: String): Result<RestoreSubscriptionResponse> =
        runCatching {
            api.restoreSubscription(RestoreSubscriptionRequest(auth0Sub))
        }
}