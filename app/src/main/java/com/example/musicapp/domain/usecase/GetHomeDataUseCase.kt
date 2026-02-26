package com.example.musicapp.domain.usecase

import com.example.musicapp.domain.model.HomeData
import com.example.musicapp.domain.repository.SongRepository

class GetHomeDataUseCase(
    private val songRepository: SongRepository

) {
    suspend operator fun invoke(): HomeData {
        return songRepository.getHomeData()
    }
}

