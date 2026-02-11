package com.example.musicapp.data.repository

import com.example.musicapp.data.api.DeezerApiService
import com.example.musicapp.domain.model.Song
import com.example.musicapp.domain.repository.SongRepository
import javax.inject.Inject

class SongRepositoryImpl @Inject constructor(
    private val deezerApiService: DeezerApiService
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
}