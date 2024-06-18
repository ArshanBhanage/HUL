package com.hul.di
import android.content.Context
import androidx.room.Room
import com.hul.sync.HulDatabase
import com.hul.sync.VisitDataDao
import com.hul.sync.VisitDataRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule {
    @Provides
    fun provideVisitDataDao(hulDatabase: HulDatabase): VisitDataDao {
        return hulDatabase.visitDataDao()
    }

    @Provides
    @Singleton
    fun provideHULDatabase(mContext: Context): HulDatabase {
        return Room.databaseBuilder(
                mContext.applicationContext,
                HulDatabase::class.java,
                "hul_database"
            ).build()
    }

    @Provides
    @Singleton
    fun provideVisitDataRepository(
        visitDataDao: VisitDataDao,
    ): VisitDataRepository {
        return VisitDataRepository(visitDataDao)
    }
}