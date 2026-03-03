package com.example.musicapp.domain.usecase

import com.example.musicapp.domain.repository.SongRepository

class AddPlaylistUseCase(
    private val songRepository: SongRepository
) {
    suspend operator fun invoke(name: String) {
        songRepository.addPlaylist(name)
    }
}