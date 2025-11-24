package com.example.appmovilfitquality.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.example.appmovilfitquality.data.localstore.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking


object MicroserviceUrls {

    private const val BASE_IP = "http://192.168.0.6"


    const val REGISTRO_USUARIO = "$BASE_IP:8020/"

    const val AUTENTICAR_USUARIO = "$BASE_IP:8021/"

    const val PRODUCTOS_TIENDA = "$BASE_IP:8022/"

    const val VENTAS = "$BASE_IP:8023/"

    const val DIRECCIONES = "$BASE_IP:8024/"
}

object RetrofitClient {


    fun createOkHttpClient(sessionManager: SessionManager): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)

            .addInterceptor { chain ->

                val token = runBlocking { sessionManager.tokenFlow.first() }
                val originalRequest = chain.request()

                val newRequest = if (!token.isNullOrBlank()) {
                    originalRequest.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                } else {
                    originalRequest
                }
                chain.proceed(newRequest)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }


    fun createRetrofitInstance(baseUrl: String, client: OkHttpClient): Retrofit {
        return Retrofit.Builder()

            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    /**
     * Función centralizada para crear la instancia de ApiService.
     */
    fun createApiService(sessionManager: SessionManager): ApiService {
        val client = createOkHttpClient(sessionManager)

        val retrofit = createRetrofitInstance(MicroserviceUrls.AUTENTICAR_USUARIO, client)
        return retrofit.create(ApiService::class.java)
    }
}