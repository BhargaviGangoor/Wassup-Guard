package com.example.wassupguard.network

import com.example.wassupguard.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ApiClient {
    private const val BASE_URL = "https://www.virustotal.com/api/v3/"

    /**
     * Creates VirusTotal API client with API key from BuildConfig
     * BuildConfig reads the key from local.properties file
     */
    fun createVirusTotalApi(): VirusTotalApi {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val authInterceptor = Interceptor { chain ->
            val apiKey = BuildConfig.VIRUSTOTAL_API_KEY
            val original = chain.request()
            val newReq = original.newBuilder()
                .addHeader("x-apikey", apiKey)
                .build()
            chain.proceed(newReq)
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(client)
            .build()

        return retrofit.create(VirusTotalApi::class.java)
    }
}


