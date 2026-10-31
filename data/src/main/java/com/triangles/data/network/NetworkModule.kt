package com.triangles.data.network

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.triangles.data.AppBuildConfig
import com.triangles.data.network.interceptors.RequestInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.ConnectionPool
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    fun OkHttpClient.Builder.applyNetworkConfig(): OkHttpClient.Builder = apply {
        callTimeout(60, TimeUnit.SECONDS)
        connectTimeout(10, TimeUnit.SECONDS)
        readTimeout(30, TimeUnit.SECONDS)
        writeTimeout(30, TimeUnit.SECONDS)
        ConnectionPool(
            maxIdleConnections = 5,
            keepAliveDuration = 90,
            timeUnit = TimeUnit.SECONDS
        )
        retryOnConnectionFailure(true)
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(appBuildConfig: AppBuildConfig): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (appBuildConfig.isDebug) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    fun provideRequestInterceptor(): RequestInterceptor = RequestInterceptor()

    @Provides
    @Singleton
    fun provideChuckerInterceptor(@ApplicationContext context: Context) =
        ChuckerInterceptor
            .Builder(context)
            .alwaysReadResponseBody(true)
            .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        requestInterceptor: RequestInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        chuckerInterceptor: ChuckerInterceptor
    ) = OkHttpClient.Builder().apply {
        applyNetworkConfig()
        addInterceptor(requestInterceptor)
        addInterceptor(chuckerInterceptor)
        addInterceptor(loggingInterceptor)
    }.build()


    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideGsonConverterFactory(gson: Gson): Converter.Factory = GsonConverterFactory.create(gson)

    @Provides
    @Singleton
    fun provideRetrofit(
        appBuildConfig: AppBuildConfig,
        factory: Converter.Factory,
        okHttpClient: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(factory)
            .baseUrl(appBuildConfig.baseApiUrl)
            .build()

}