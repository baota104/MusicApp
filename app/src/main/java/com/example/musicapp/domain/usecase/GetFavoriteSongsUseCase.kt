package com.example.musicapp.domain.usecase

import com.example.musicapp.domain.model.Song
import com.example.musicapp.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow

class GetFavoriteSongsUseCase(private val repository: SongRepository) {
    operator fun invoke(): Flow<List<Song>> {
        return repository.getFavoriteSongs()
    }
}