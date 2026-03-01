package com.example.musicapp.domain.usecase

import com.example.musicapp.domain.repository.SongRepository

class CheckFavoriteUseCase (
    private val repository: SongRepository
){
    suspend operator fun invoke(songId: Long): Boolean {
        return repository.isFavorite(songId)
    }
}

