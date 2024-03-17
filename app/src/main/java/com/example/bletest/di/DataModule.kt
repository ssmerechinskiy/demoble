package com.example.bletest.di

import android.content.Context
import androidx.room.Room
import com.example.bletest.datalayer.db.AppDatabase
import com.example.bletest.datalayer.db.DBDataSource
import com.example.bletest.datalayer.db.DBDataSourceImpl
import com.example.bletest.datalayer.repository.DataRepository
import com.example.bletest.datalayer.repository.DataRepositoryImpl
import com.example.bletest.datalayer.network.NetworkDataSource
import com.example.bletest.datalayer.network.NetworkDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    @Singleton
    abstract fun bindNetworkDataSource(
        networkDataSourceImpl: NetworkDataSourceImpl
    ): NetworkDataSource

    @Binds
    @Singleton
    abstract fun bindDataRepository(
        dataRepositoryImpl: DataRepositoryImpl
    ): DataRepository

    @Binds
    @Singleton
    abstract fun bindDBDataSource(
        dbDataSourceImpl: DBDataSourceImpl
    ): DBDataSource

}

@Module
@InstallIn(SingletonComponent::class)
object ProvideDataModule {
    @Provides
    @Singleton
    fun provideRoom(@ApplicationContext appContext: Context) : AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java, "device_db"
        ).build()
    }
}