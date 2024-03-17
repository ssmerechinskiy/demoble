package com.example.bletest.di

import com.example.bletest.datalayer.network.DeviceAPI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @Provides
    fun provideOkHttpClient() : OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
        return OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()
    }

    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient) : Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://cosmo-api.develop-sr3snxi-x6u2x52ooksf4.de-2.platformsh.site/test/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideDeviceAPI(retrofit: Retrofit) : DeviceAPI {
        return retrofit.create(DeviceAPI::class.java)
    }

}