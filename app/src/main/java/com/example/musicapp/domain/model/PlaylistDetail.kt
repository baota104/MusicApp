package com.example.musicapp.domain.model

// Lớp này hoàn toàn không có dấu vết nào của Room Database
data class PlaylistDetail(
    val playlist: Playlist,
    val songs: List<Song>
)