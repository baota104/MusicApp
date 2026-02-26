package com.example.musicapp

import android.app.Application
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicapp.data.api.DeezerApiService
import com.example.musicapp.data.repository.SongRepositoryImpl
import com.example.musicapp.domain.repository.SongRepository
import dagger.hilt.android.HiltAndroidApp
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MusicApplication : Application(){

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.deezer.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: DeezerApiService by lazy {
        retrofit.create(DeezerApiService::class.java)
    }

    val songRepository: SongRepository by lazy {
        SongRepositoryImpl( apiService)
    }
    val exoPlayer: ExoPlayer by lazy {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
    }

}

