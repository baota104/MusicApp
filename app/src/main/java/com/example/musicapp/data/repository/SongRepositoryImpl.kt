package com.example.musicapp.data.repository

import com.example.musicapp.data.api.DeezerApiService
import com.example.musicapp.data.local.dao.MusicDao
import com.example.musicapp.data.local.entity.SongEntity
import com.example.musicapp.domain.model.Genre
import com.example.musicapp.domain.model.HomeData
import com.example.musicapp.domain.model.HomeItem
import com.example.musicapp.domain.model.ItemType
import com.example.musicapp.domain.model.Song
import com.example.musicapp.domain.repository.SongRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SongRepositoryImpl (
    private val deezerApiService: DeezerApiService,
    private val musicDao: MusicDao
): SongRepository {
    override suspend fun getTopSong(): List<Song> {
        // api lay du lieu tho
        val response = deezerApiService.getTopSongs()

        // chuyen du dto sang domain model
        // Tại sao phải chuyển? Để nếu API đổi tên trường, ta chỉ cần sửa ở đây, UI không bị ảnh hưởng.
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
        return musicDao.isSongFavorite(songId)
    }

    override suspend fun toggleFavorite(song: Song): Boolean {
        val isFav = musicDao.isSongFavorite(song.id)

        val entity = SongEntity(
            id = song.id,
            title = song.title,
            artistName = song.artistName,
            coverUrl = song.coverUrl,
            sourceUrl = song.sourceUrl,
            duration = song.duration
        )

        return if (isFav) {
            musicDao.deleteFavoriteSong(entity)
            false
        } else {
            musicDao.insertFavoriteSong(entity)
            true
        }
    }
    override fun getFavoriteSongs(): Flow<List<Song>> {
        return musicDao.getAllFavoriteSongs().map { entities ->
            entities.map { entity ->
                    Song(
                    id = entity.id,
                    title = entity.title,
                    artistName = entity.artistName,
                    coverUrl = entity.coverUrl,
                    sourceUrl = entity.sourceUrl,
                    duration = entity.duration
                )
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
}