package com.hul.di

/**
 * Created by Nitin Chorge on 23-11-2020.
 */
import com.hul.storage.SharedPreferencesStorage
import com.hul.storage.Storage
import dagger.Binds
import dagger.Module

@Module
abstract class StorageModule {

    @Binds
    abstract fun provideStorage(storage: SharedPreferencesStorage): Storage
}