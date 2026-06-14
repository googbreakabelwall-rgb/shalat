package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FavoriteVerse::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val favoriteVerseDao: FavoriteVerseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "noor_qibla_quran_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
