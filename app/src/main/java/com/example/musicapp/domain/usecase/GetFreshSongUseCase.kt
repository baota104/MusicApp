package com.example.musicapp.domain.usecase

import com.example.musicapp.domain.model.Song
import com.example.musicapp.domain.repository.SongRepository

class GetFreshSongUseCase(private val repository: SongRepository) {
    suspend operator fun invoke(songId: Long): Song? {
        return repository.getFreshSongById(songId)
    }
}