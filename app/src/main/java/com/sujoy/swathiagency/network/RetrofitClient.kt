package com.sujoy.swathiagency.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthInterceptor(private val accessToken: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val newRequest: Request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $accessToken")
            .build()
        return chain.proceed(newRequest)
    }
}

object RetrofitClient {
    private var client: OkHttpClient? = null

    fun getRetrofitInstance(accessToken: String): Retrofit {
        if (client == null) {
            client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(accessToken))
//            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
                .build()
        }

        return Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .client(client!!)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getRetrofitInstanceWithoutAccessToken(): Retrofit {
        if (client == null) {
            client = OkHttpClient.Builder()
                .build()
        }

        return Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .client(client!!)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}