package com.hul.di
import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hul.sync.HulDatabase
import com.hul.sync.SocietyVisitDataDao
import com.hul.sync.SocietyVisitDataRepository
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
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
            CREATE TABLE IF NOT EXISTS society_visit_data_table (
                id INTEGER PRIMARY KEY NOT NULL,
                visitNumber INTEGER NOT NULL,
                locationId TEXT NOT NULL,
                locationName TEXT NOT NULL,
                floor TEXT NOT NULL,
                flatNumber TEXT NOT NULL,
                wingNumber TEXT NOT NULL,
                jsonData TEXT NOT NULL
            )
        """.trimIndent())
            }
        }
        return Room.databaseBuilder(
                mContext.applicationContext,
                HulDatabase::class.java,
                "hul_database"
            ).addMigrations(MIGRATION_1_2).build()
    }

    @Provides
    @Singleton
    fun provideVisitDataRepository(
        visitDataDao: VisitDataDao,
    ): VisitDataRepository {
        return VisitDataRepository(visitDataDao)
    }

    @Provides
    fun provideSocietyVisitDataDao(hulDatabase: HulDatabase): SocietyVisitDataDao {
        return hulDatabase.societyVisitDataDao()
    }

    @Provides
    @Singleton
    fun provideSocietyVisitDataRepository(
        societyVisitDataDao: SocietyVisitDataDao,
    ): SocietyVisitDataRepository {
        return SocietyVisitDataRepository(societyVisitDataDao)
    }
}