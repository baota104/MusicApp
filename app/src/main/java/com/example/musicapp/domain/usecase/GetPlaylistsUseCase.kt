package com.example.musicapp.domain.usecase

import com.example.musicapp.domain.model.Playlist
import com.example.musicapp.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow

class GetPlaylistsUseCase(private val repository: SongRepository) {
    operator fun invoke(): Flow<List<Playlist>> {
        return repository.getAllPlaylists()
    }
}