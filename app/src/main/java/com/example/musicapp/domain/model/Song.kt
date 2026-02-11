package com.example.musicapp.domain.model

data class Song(
    val id: Long,
    val title: String,
    val artistName: String,
    val coverUrl: String,
    val sourceUrl: String, // Link nhạc mp3 (preview)
    val duration: Int
)
