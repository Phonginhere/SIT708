package com.example.llm61.network

import com.example.llm61.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class GeminiRepository {

    // OkHttp client with logging (helps debug API calls in Logcat)
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Build Retrofit once and reuse it
    private val service: GeminiService = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GeminiService::class.java)

    // The function the rest of the app uses.
    // Returns Result<String> so callers can handle success vs failure cleanly.
    suspend fun generate(prompt: String): Result<String> = runCatching {
        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(prompt))))
        )
        val response = service.generateContent(BuildConfig.GEMINI_API_KEY, request)

        // Walk the nested response structure to get the text
        response.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text
            ?: throw IllegalStateException("Empty response from Gemini")
    }
}