package com.example.musicapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: Long, // Lấy ID của Deezer làm khóa chính luôn
    val title: String,
    val artistName: String,
    val coverUrl: String,
    val sourceUrl: String,
    val duration: Int,
    val addedAt: Long = System.currentTimeMillis() // Thời gian thả tim để sắp xếp mới nhất
)