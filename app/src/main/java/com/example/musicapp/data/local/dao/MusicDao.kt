package com.example.musicapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.musicapp.data.local.entity.PlaylistEntity
import com.example.musicapp.data.local.entity.PlaylistSongCrossRef
import com.example.musicapp.data.local.entity.PlaylistWithSongs
import com.example.musicapp.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongToCache(song: SongEntity)

    @Delete
    suspend fun deleteFavoriteSong(song: SongEntity) // ti nua sua bo favorite

    @Query("SELECT EXISTS(SELECT 1 FROM songs WHERE id = :songId)")
    suspend fun isSongInCache(songId: Long): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND songId = :songId)")
    suspend fun isSongInPlaylist(playlistId: Long, songId: Long): Boolean

    @Query("SELECT * FROM songs ORDER BY addedAt DESC")
    fun getAllFavoriteSongs(): Flow<List<SongEntity>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity):Long

    @Query("SELECT * FROM playlists WHERE isSystem = 0 ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSongToPlaylist(crossRef: PlaylistSongCrossRef)

    @androidx.room.Transaction
    @Query("SELECT * FROM playlists WHERE playlistId = :playlistId")
    fun getPlaylistWithSongs(playlistId: Long): Flow<PlaylistWithSongs>

    @Query("UPDATE playlists SET imageUrl = :imageUrl WHERE playlistId = :playlistId")
    suspend fun updatePlaylistImage(playlistId: Long, imageUrl: String)

    @Query("SELECT playlistId FROM playlists WHERE isSystem = 1 LIMIT 1")
    suspend fun getSystemFavoritePlaylistId(): Long?

    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun deleteSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("""
        DELETE FROM songs
        WHERE id NOT IN (SELECT songId FROM playlist_song_cross_ref)
    """)
    suspend fun clearOrphanSongs()
}