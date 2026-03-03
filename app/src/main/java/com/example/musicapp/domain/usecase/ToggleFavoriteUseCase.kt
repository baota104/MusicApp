package com.example.musicapp.domain.usecase

import com.example.musicapp.domain.model.Song
import com.example.musicapp.domain.repository.SongRepository

class ToggleFavoriteUseCase(private val repository: SongRepository) {
    suspend operator fun invoke(song: Song, isFavorite: Boolean): Boolean {
        return repository.toggleFavorite(song,isFavorite)
    }
}