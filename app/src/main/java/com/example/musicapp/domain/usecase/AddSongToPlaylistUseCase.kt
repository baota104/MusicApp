package com.example.musicapp.domain.usecase

import com.example.musicapp.data.local.entity.PlaylistSongCrossRef
import com.example.musicapp.domain.model.Song
import com.example.musicapp.domain.repository.SongRepository
import kotlinx.coroutines.flow.first

class AddSongToPlaylistUseCase(
    private val repository: SongRepository

) {
    suspend operator fun invoke(playlistId: Long, song: Song) {

        repository.addSongToPlaylist(playlistId,song)

        val playlistDetail = repository.getPlaylistWithSongs(playlistId).first()
        if (playlistDetail.playlist.imageUrl == null) {
            repository.updatePlaylistImage(playlistId, song.coverUrl)
        }
    }
}
