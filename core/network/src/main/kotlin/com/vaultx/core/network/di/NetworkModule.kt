package com.vaultx.core.network.di

import com.vaultx.core.network.BuildConfig
import com.vaultx.core.network.api.MarketFeedApi
import com.vaultx.core.network.interceptor.SimulatedMarketFeedInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://api.vaultx.io/"
    private const val TIMEOUT_SECONDS = 30L

    @Provides
    @Singleton
    fun provideNetworkJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    @Provides
    @Singleton
    fun provideSimulatedMarketFeedInterceptor(
        networkJson: Json,
    ): SimulatedMarketFeedInterceptor = SimulatedMarketFeedInterceptor(networkJson)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        simulatedMarketFeedInterceptor: SimulatedMarketFeedInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(simulatedMarketFeedInterceptor)
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideMarketFeedApi(
        okHttpClient: OkHttpClient,
        networkJson: Json,
    ): MarketFeedApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(networkJson.asConverterFactory(contentType))
            .build()
            .create(MarketFeedApi::class.java)
    }
}
