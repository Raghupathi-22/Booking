package com.hyderabad.booking.common.network

import com.hyderabad.booking.common.model.RefreshTokenRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val AUTH_PATH = "/api/v1/auth/"

    fun create(baseUrl: String, tokenStore: TokenStore, debug: Boolean): BookingApi {
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val converter = MoshiConverterFactory.create(moshi)

        val logging = HttpLoggingInterceptor().apply {
            level = if (debug) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            redactHeader("Authorization")
        }

        // Separate client (no auth interceptor / authenticator) used only to refresh the token.
        val refreshApi = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(OkHttpClient.Builder().addInterceptor(logging).build())
            .addConverterFactory(converter)
            .build()
            .create(BookingApi::class.java)

        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val token = tokenStore.accessToken
            val request = if (token != null && !original.url.encodedPath.startsWith(AUTH_PATH)) {
                original.newBuilder().header("Authorization", "Bearer $token").build()
            } else {
                original
            }
            chain.proceed(request)
        }

        // On 401: try once to get a new access token with the refresh token, then retry.
        val authenticator = Authenticator { _, response ->
            if (response.request.url.encodedPath.startsWith(AUTH_PATH)) return@Authenticator null
            synchronized(tokenStore) {
                val sentToken = response.request.header("Authorization")?.removePrefix("Bearer ")
                val currentToken = tokenStore.accessToken
                // Another request already refreshed the token: just retry with the new one.
                if (currentToken != null && currentToken != sentToken) {
                    return@Authenticator response.request.newBuilder()
                        .header("Authorization", "Bearer $currentToken").build()
                }
                val refresh = tokenStore.refreshToken ?: run {
                    tokenStore.clear()
                    return@Authenticator null
                }
                val refreshed = try {
                    refreshApi.refreshCall(RefreshTokenRequest(refresh)).execute()
                } catch (e: Exception) {
                    return@Authenticator null // network problem: keep the session, fail this call
                }
                val body = refreshed.body()
                if (refreshed.isSuccessful && body != null) {
                    tokenStore.save(body)
                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${body.accessToken}").build()
                } else {
                    tokenStore.clear()
                    null
                }
            }
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .authenticator(authenticator)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(converter)
            .build()
            .create(BookingApi::class.java)
    }
}
