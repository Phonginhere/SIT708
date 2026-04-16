package com.example.pass51_istream.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class, PlaylistItem::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun playlistDao(): PlaylistDao

    companion object { // Singleton instance of the database
        @Volatile // Ensures visibility of changes to INSTANCE across threads
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) { // Ensure only one instance of the database is created
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "istream_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}