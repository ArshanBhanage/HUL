package com.hul.sync

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [VisitDataTable::class], version = 1, exportSchema = false)
abstract class HulDatabase : RoomDatabase() {
    abstract fun visitDataDao(): VisitDataDao

//    companion object {
//        @Volatile
//        private var INSTANCE: HulDatabase? = null
//
//        fun getDatabase(context: Context): HulDatabase {
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    HulDatabase::class.java,
//                    "sync_database"
//                ).build()
//                INSTANCE = instance
//                instance
//            }
//        }
//    }
}
