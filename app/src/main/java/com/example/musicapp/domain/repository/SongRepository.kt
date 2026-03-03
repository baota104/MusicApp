package com.example.musicapp.domain.repository

import com.example.musicapp.data.local.entity.PlaylistSongCrossRef
import com.example.musicapp.data.local.entity.PlaylistWithSongs
import com.example.musicapp.domain.model.Genre
import com.example.musicapp.domain.model.HomeData
import com.example.musicapp.domain.model.Playlist
import com.example.musicapp.domain.model.PlaylistDetail
import com.example.musicapp.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {

    suspend fun getTopSong(): List<Song>
    suspend fun getHomeData(): HomeData
    suspend fun getGenres(): List<Genre>
    suspend fun searchSongs(query: String): List<Song>
    suspend fun isFavorite(songId: Long): Boolean
    suspend fun toggleFavorite(song: Song,isFavorite: Boolean): Boolean
    fun getFavoriteSongs(): Flow<List<Song>>
    suspend fun getFreshSongById(songId: Long): Song?
    suspend fun addPlaylist(name: String)
    fun getAllPlaylists(): Flow<List<Playlist>>
    suspend fun addSongToPlaylist(playlistId:Long,song:Song)
    suspend fun updatePlaylistImage(playlistId: Long, imageUrl: String)
    fun getPlaylistWithSongs(playlistId: Long): Flow<PlaylistDetail>
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)


}