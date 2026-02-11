package com.example.musicapp.domain.repository

import com.example.musicapp.domain.model.Song

interface SongRepository {

    suspend fun getTopSong(): List<Song>
}