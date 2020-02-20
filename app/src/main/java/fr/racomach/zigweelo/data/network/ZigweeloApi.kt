package fr.racomach.zigweelo.data.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface ZigweeloApi {

    companion object {

        fun create(baseUrl: String = "https://www.brav0.space/zigweelo-api/"): ZigweeloApi {
            val okHttpClientBuilder = OkHttpClient.Builder()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder().apply {
                baseUrl(baseUrl)
                client(okHttpClientBuilder.build())
                addConverterFactory(MoshiConverterFactory.create(moshi))
            }.build()

            return retrofit.create(ZigweeloApi::class.java)
        }
    }

    @POST("users/anonymous")
    suspend fun createAnonymousUser(@Body user: UserRequest): Response<UserResponse>
}
