package com.example.musicapp.domain.model

enum class ItemType {
    PLAYLIST, RADIO, ALBUM,SONG
}

data class HomeItem(
    val id: Long,
    val title: String,
    val imageUrl: String,
    val type: ItemType
)

data class HomeData(
    val continueListening: List<HomeItem>,
    val topMixes: List<HomeItem>,
    val playlists: List<Song>,
)