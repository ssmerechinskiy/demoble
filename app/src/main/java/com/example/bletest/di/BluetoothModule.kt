package com.example.bletest.di

import android.content.Context
import com.example.bletest.bt.BLEScanner
import com.example.bletest.bt.ConnectionManagerProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProvideBTModule {
    @Provides
    @Singleton
    fun provideScanner(@ApplicationContext appContext: Context) : BLEScanner {
        return BLEScanner(appContext)
    }

    @Provides
    @Singleton
    fun provideConnectionProvider(
        @ApplicationContext appContext : Context,
        @IoDispatcher dispatcher : CoroutineDispatcher,
        appScope : CoroutineScope
    ) : ConnectionManagerProvider {
        return ConnectionManagerProvider(appContext, dispatcher, appScope)
    }
}