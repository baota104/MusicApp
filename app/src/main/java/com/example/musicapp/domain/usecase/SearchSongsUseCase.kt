package com.example.musicapp.domain.usecase

import com.example.musicapp.domain.model.Song
import com.example.musicapp.domain.repository.SongRepository

class SearchSongsUseCase(
    private val repository: SongRepository
) {
    suspend operator fun invoke(query: String): List<Song> {
        if (query.trim().isEmpty()) {
            return emptyList()
        }
        return repository.searchSongs(query)
    }
}