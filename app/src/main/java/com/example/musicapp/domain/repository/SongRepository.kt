package com.example.musicapp.domain.repository

import com.example.musicapp.domain.model.Genre
import com.example.musicapp.domain.model.HomeData
import com.example.musicapp.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {

    suspend fun getTopSong(): List<Song>
    suspend fun getHomeData(): HomeData
    suspend fun getGenres(): List<Genre>
    suspend fun searchSongs(query: String): List<Song>
    suspend fun isFavorite(songId: Long): Boolean
    suspend fun toggleFavorite(song: Song): Boolean
    fun getFavoriteSongs(): Flow<List<Song>>
    suspend fun getFreshSongById(songId: Long): Song?
}