package com.example.musicapp.data.repository

import com.example.musicapp.data.api.DeezerApiService
import com.example.musicapp.data.local.dao.MusicDao
import com.example.musicapp.data.local.entity.PlaylistEntity
import com.example.musicapp.data.local.entity.PlaylistSongCrossRef
import com.example.musicapp.data.local.entity.PlaylistWithSongs
import com.example.musicapp.data.local.entity.SongEntity
import com.example.musicapp.domain.model.Genre
import com.example.musicapp.domain.model.HomeData
import com.example.musicapp.domain.model.HomeItem
import com.example.musicapp.domain.model.ItemType
import com.example.musicapp.domain.model.Playlist
import com.example.musicapp.domain.model.PlaylistDetail
import com.example.musicapp.domain.model.Song
import com.example.musicapp.domain.repository.SongRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SongRepositoryImpl (
    private val deezerApiService: DeezerApiService,
    private val musicDao: MusicDao
): SongRepository {
    override suspend fun getTopSong(): List<Song> {
        val response = deezerApiService.getTopSongs()
        return response.data.map { dto ->
            Song(
                id = dto.id,
                title = dto.title,
                artistName = dto.artist.name,
                coverUrl = dto.album.coverUrl,
                sourceUrl = dto.previewUrl,
                duration = dto.duration
            )
        }
    }

    override suspend fun getHomeData(): HomeData {
        return coroutineScope { // Tạo scope để chạy song song
            try {
                // 1. Gửi 3 yêu cầu API đi cùng một lúc
                val playlistsDeferred = async { deezerApiService.getTopPlaylists(limit = 6) } // Lấy 6 cái cho 2 cột 3 dòng
                val radiosDeferred = async { deezerApiService.getTopRadios(limit = 10) }
                val SongDeferred = async { getTopSong() }

                // 2. Chờ cả 3 thằng trả kết quả về (await)
                val playlistsResponse = playlistsDeferred.await()
                val radiosResponse = radiosDeferred.await()
                val SongResponse = SongDeferred.await()

                // 3. MAPPING: Biến DTO thành HomeItem
                val continueListeningList = playlistsResponse.data?.map { dto ->
                    HomeItem(
                        id = dto.id,
                        title = dto.title ?: "Unknown Playlist",
                        imageUrl = dto.pictureUrl ?: "",
                        type = ItemType.PLAYLIST
                    )
                } ?: emptyList()

                val topMixesList = radiosResponse.data?.map { dto ->
                    HomeItem(
                        id = dto.id,
                        title = dto.title ?: "Unknown Mix",
                        imageUrl = dto.pictureUrl ?: "",
                        type = ItemType.RADIO
                    )
                } ?: emptyList()


                // 4. Đóng gói vào HomeData và trả về
                HomeData(
                    continueListening = continueListeningList,
                    topMixes = topMixesList,
                    playlists = SongResponse
                )

            } catch (e: Exception) {
                android.util.Log.e("DEBUG_API", "Bị lỗi khi gọi API Home: ${e.message}")
                HomeData(emptyList(), emptyList(), emptyList())
            }
        }
    }
    override suspend fun getGenres(): List<Genre> {
        return try {
            val response = deezerApiService.getGenres()

            response.data?.mapNotNull { dto ->
                // Lọc bỏ thể loại "All" (id = 0)
                if (dto.id == 0L) return@mapNotNull null

                Genre(
                    id = dto.id,
                    name = dto.name ?: "Unknown",
                    pictureUrl = dto.pictureUrl ?: ""
                )
            } ?: emptyList()

        } catch (e: Exception) {
            android.util.Log.e("DEBUG_API", "Lỗi lấy Genres: ${e.message}")
            emptyList()
        }
    }

    override suspend fun searchSongs(query: String): List<Song> {
        return try {
            val response = deezerApiService.searchSongs(query)

            // Map từ DTO sang Domain Model giống hệt getTopSong()
            response.data.map { dto ->
                Song(
                    id = dto.id,
                    title = dto.title,
                    artistName = dto.artist.name,
                    coverUrl = dto.album.coverUrl,
                    sourceUrl = dto.previewUrl, // Link nhạc 30s
                    duration = dto.duration
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("DEBUG_API", "Lỗi tìm kiếm bài hát: ${e.message}")
            emptyList() // Lỗi thì trả về mảng rỗng để app không bị crash
        }
    }

    override suspend fun isFavorite(songId: Long): Boolean {
        val favPlaylistId = getOrCreateFavoritePlaylistId()
        return musicDao.isSongInPlaylist(favPlaylistId, songId)
    }

    override suspend fun toggleFavorite(song: Song,isFavorite: Boolean): Boolean {
       try {
            val favPlaylistId = getOrCreateFavoritePlaylistId()
            if (isFavorite) {
                val songEntity = SongEntity(
                    id = song.id, title = song.title, artistName = song.artistName,
                    coverUrl = song.coverUrl, sourceUrl = song.sourceUrl, duration = song.duration
                )
                musicDao.insertSongToCache(songEntity)

                val crossRef = PlaylistSongCrossRef(favPlaylistId, song.id)
                musicDao.insertSongToPlaylist(crossRef)
                return true
            } else {
                musicDao.deleteSongFromPlaylist(favPlaylistId, song.id)
                musicDao.clearOrphanSongs()
                return false
            }

        }
       catch (e: Exception){
           android.util.Log.e("DEBUG_API", "Lỗi thêm/xóa bài hát yêu thích: ${e.message}")
           return false
       }
    }
    override fun getFavoriteSongs(): Flow<List<Song>> {
        return kotlinx.coroutines.flow.flow {
            val favId = getOrCreateFavoritePlaylistId()
            musicDao.getPlaylistWithSongs(favId).collect { entityData ->
                val songs = entityData.songs.map { songEntity ->
                 Song(
                        id = songEntity.id, title = songEntity.title, artistName = songEntity.artistName,
                        coverUrl = songEntity.coverUrl, sourceUrl = songEntity.sourceUrl, duration = songEntity.duration
                    )
                }
                emit(songs)
            }
        }
    }
    override suspend fun getFreshSongById(songId: Long): Song? {
        return try {
            val dto = deezerApiService.getTrackById(songId)
            Song(
                id = dto.id,
                title = dto.title,
                artistName = dto.artist?.name ?: "Unknown",
                coverUrl = dto.album?.coverUrl ?: "",
                sourceUrl = dto.previewUrl ?: "",
                duration = dto.duration
            )
        } catch (e: Exception) {
            android.util.Log.e("DEBUG_API", "Lỗi lấy bài hát mới: ${e.message}")
            null
        }
    }

    override suspend fun addPlaylist(name: String) {
        val entity = PlaylistEntity(name = name)
        musicDao.insertPlaylist(entity)
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return musicDao.getAllPlaylists().map { entities ->
            entities.map {
                Playlist(
                    id = it.playlistId,
                    imageUrl = it.imageUrl,
                    name = it.name
                )
            }
        }
    }

    override suspend fun addSongToPlaylist(playlistId: Long, song: Song) {
        val isincache = musicDao.isSongInCache(song.id)
        if(!isincache){
            val songEntity = SongEntity(
                id = song.id,
                title = song.title,
                artistName = song.artistName,
                coverUrl = song.coverUrl,
                sourceUrl = song.sourceUrl,
                duration = song.duration
            )
            musicDao.insertSongToCache(songEntity)
        }
        val crossRef = PlaylistSongCrossRef(playlistId = playlistId, songId = song.id)
        musicDao.insertSongToPlaylist(crossRef)

    }

    override suspend fun updatePlaylistImage(playlistId: Long, imageUrl: String) {
        musicDao.updatePlaylistImage(playlistId, imageUrl)
    }

    override fun getPlaylistWithSongs(playlistId: Long): Flow<PlaylistDetail> {
        return musicDao.getPlaylistWithSongs(playlistId).map { entityData ->
            PlaylistDetail(
                playlist = Playlist(
                    id = entityData.playlist.playlistId,
                    name = entityData.playlist.name,
                    imageUrl = entityData.playlist.imageUrl
                ),
                songs = entityData.songs.map { songEntity ->
                    Song(
                        id = songEntity.id,
                        title = songEntity.title,
                        artistName = songEntity.artistName,
                        coverUrl = songEntity.coverUrl,
                        sourceUrl = songEntity.sourceUrl,
                        duration = songEntity.duration
                    )
                }
            )
        }
    }
    private suspend fun getOrCreateFavoritePlaylistId(): Long {
        val existingId = musicDao.getSystemFavoritePlaylistId()
        if (existingId != null) return existingId
        val systemPlaylist = PlaylistEntity(
            name = "Your Favorite Songs",
            isSystem = true
        )
        return musicDao.insertPlaylist(systemPlaylist)
    }
    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        musicDao.deleteSongFromPlaylist(playlistId, songId)
        musicDao.clearOrphanSongs()
    }

}