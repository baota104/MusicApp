package com.example.musicapp.domain.usecase

import com.example.musicapp.domain.repository.SongRepository

class RemoveSongFromPlaylistUseCase(private val repository: SongRepository) {
    suspend operator fun invoke(playlistId: Long, songId: Long) {
        repository.removeSongFromPlaylist(playlistId, songId)
    }
}