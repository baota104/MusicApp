package com.example.musicapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.musicapp.data.local.dao.MusicDao
import com.example.musicapp.data.local.entity.PlaylistEntity
import com.example.musicapp.data.local.entity.SongEntity

// Khai báo các bảng có trong Database
@Database(
    entities = [SongEntity::class, PlaylistEntity::class],
    version = 1, // Nếu sau này sửa bảng, phải tăng số này lên
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun musicDao(): MusicDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "music_app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}