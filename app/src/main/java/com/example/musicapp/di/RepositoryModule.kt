package com.example.musicapp.di

import com.example.musicapp.data.repository.SongRepositoryImpl
import com.example.musicapp.domain.repository.SongRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds // Dùng @Binds để gắn kết Interface với Implementation
    @Singleton
    abstract fun bindSongRepository(
        songRepositoryImpl: SongRepositoryImpl
    ): SongRepository
}