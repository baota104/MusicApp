package com.example.musicapp.di

import com.example.musicapp.data.api.DeezerApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)// module nay song cung vong doi ung dung
object NetworkModule {
    @Provides
    @Singleton
    fun provideRetrofit() : Retrofit{
        return Retrofit.Builder()
            .baseUrl("https://api.deezer.com/")
            .addConverterFactory(GsonConverterFactory.create()) // dung gson de convert json sang object
            .build()
    }

    @Provides
    @Singleton
    fun provideDeezerApiService(retrofit: Retrofit): DeezerApiService {
        return retrofit.create(DeezerApiService::class.java)
    }
}