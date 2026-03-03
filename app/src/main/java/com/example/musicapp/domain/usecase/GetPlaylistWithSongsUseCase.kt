package com.example.musicapp.domain.usecase

import com.example.musicapp.data.local.entity.PlaylistWithSongs
import com.example.musicapp.domain.model.PlaylistDetail
import com.example.musicapp.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow

class GetPlaylistWithSongsUseCase(
    private val repository: SongRepository
) {
    operator fun invoke(playlistId: Long): Flow<PlaylistDetail> {
        return repository.getPlaylistWithSongs(playlistId)
    }
}