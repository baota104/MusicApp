package com.example.musicapp.domain.usecase

import com.example.musicapp.domain.model.Song
import com.example.musicapp.domain.repository.SongRepository
import javax.inject.Inject

class GetTopSongsUseCase (
    private val songRepository: SongRepository
) {
    suspend operator fun invoke(): List<Song> {
        // co the xu li logic them o day sau : loc , sap xep, ....

        return songRepository.getTopSong()
    }
}