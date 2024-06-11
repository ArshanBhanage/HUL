package com.hul.di

import com.hul.HULApplication
import com.hul.api.ApiInterface
import com.hul.user.UserInfo
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Created by Nitin Chorge on 23-11-2020.
 */
@Module
class NetworkModule {

    @Singleton
    @Provides
    fun provideRetrofit(userInfo: UserInfo,): Retrofit {
        val retrofit: Retrofit
        val mOkHttpClient: OkHttpClient
        if (HULApplication.IS_DEBUG) {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            mOkHttpClient = OkHttpClient.Builder().addInterceptor(logging)
                .connectTimeout(160, TimeUnit.SECONDS)
                .readTimeout(160L, TimeUnit.SECONDS)
                .writeTimeout(160L, TimeUnit.SECONDS)
                .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                    val original = chain.request()
                    val request = original.newBuilder()
                        .header("Content-Type", "application/json")
                        .header("Authorization", userInfo.authToken)
                        .method(original.method, original.body)
                        .build()
                    chain.proceed(request)
                }).build()
        } else {
            mOkHttpClient = OkHttpClient.Builder()
                .connectTimeout(160, TimeUnit.SECONDS)
                .readTimeout(160L, TimeUnit.SECONDS)
                .writeTimeout(160L, TimeUnit.SECONDS)
                .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                    val original = chain.request()
                    val request = original.newBuilder()
                        .header("Content-Type", "application/json")
                        .header("Authorization", userInfo.authToken)
                        .method(original.method, original.body)
                        .build()
                    chain.proceed(request)
                }).build()
        }
        retrofit = Retrofit.Builder().client(mOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).baseUrl(ApiInterface.BASE_URL)
            .build()

        return retrofit
    }
}