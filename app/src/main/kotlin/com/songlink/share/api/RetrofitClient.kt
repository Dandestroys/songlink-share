package com.songlink.share.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Singleton that provides a configured [SonglinkApi] Retrofit instance.
 *
 * Configuration highlights:
 * - Base URL points to the Songlink / Odesli public API.
 * - kotlinx.serialization is used for JSON parsing (via the Jakewharton converter).
 * - Unknown JSON fields are ignored so new API fields don't break the app.
 * - OkHttp timeouts are generous because the API may need to scrape several
 *   music platforms before returning a response.
 * - HTTP logging is enabled (BODY level) to aid debugging in development;
 *   this can be switched to NONE for release builds via BuildConfig if needed.
 */
object RetrofitClient {

    private const val BASE_URL = "https://api.song.link/"

    /**
     * Lenient JSON parser that silently ignores fields not present in our data
     * classes. This future-proofs the app against API additions.
     */
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)   // Songlink may be slow resolving cross-platform
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(
            json.asConverterFactory("application/json; charset=UTF8".toMediaType())
        )
        .build()

    /** Ready-to-use API interface. Call [SonglinkApi.getLinks] directly. */
    val api: SonglinkApi = retrofit.create(SonglinkApi::class.java)
}
