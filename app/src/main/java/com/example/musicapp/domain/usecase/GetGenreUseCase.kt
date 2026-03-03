package com.example.musicapp.domain.usecase

import com.example.musicapp.domain.model.Genre
import com.example.musicapp.domain.repository.SongRepository

class GetGenreUseCase(
    private val repository: SongRepository
) {
    suspend operator fun invoke(): List<Genre> {
        return repository.getGenres()
    }
}


