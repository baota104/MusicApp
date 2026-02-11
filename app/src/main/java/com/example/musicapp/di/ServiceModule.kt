package com.example.musicapp.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent // <-- ĐỔI CÁI NÀY
import javax.inject.Singleton // <-- ĐỔI CÁI NÀY

@Module
@InstallIn(SingletonComponent::class) // 1. Đổi sang SingletonComponent (Sống toàn App)
object ServiceModule {

    @Singleton // 2. Đổi sang Singleton
    @Provides
    fun provideAudioAttributes(): AudioAttributes =
        AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

    @Singleton // 3. Đổi sang Singleton: Chỉ có 1 Player duy nhất trong toàn bộ App
    @Provides
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes
    ): ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(audioAttributes, true)
        .setHandleAudioBecomingNoisy(true)
        .build()
}