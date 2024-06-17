package com.hul.di

import android.content.Context
import com.hul.HULApplication
import com.hul.api.ApiInterface
import com.hul.sync.SyncDataDao
import com.hul.sync.SyncDataRepository
import com.hul.sync.SyncDataViewModelFactory
import com.hul.sync.SyncDatabase
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

@Module
class SyncModule {
    @Provides
    @Singleton
    fun provideSyncDataViewModelFactory(repository: SyncDataRepository): SyncDataViewModelFactory {
        return SyncDataViewModelFactory(repository)
    }

    @Provides
    @Singleton
    fun provideSyncDatabase(context: Context): SyncDatabase {
        return SyncDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideSyncDataDao(syncDatabase: SyncDatabase): SyncDataDao {
        return syncDatabase.syncDataDao()
    }

    @Provides
    @Singleton
    fun provideSyncDataRepository(syncDataDao: SyncDataDao): SyncDataRepository {
        return SyncDataRepository(syncDataDao)
    }
}