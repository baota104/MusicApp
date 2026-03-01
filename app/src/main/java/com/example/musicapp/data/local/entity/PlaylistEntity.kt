package com.example.musicapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val playlistId: Long = 0, // ID tự động tăng
    val name: String,
    val numberOfSongs: Int,
    val createdAt: Long = System.currentTimeMillis()
)