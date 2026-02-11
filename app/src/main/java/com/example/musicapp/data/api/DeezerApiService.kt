package com.example.musicapp.data.api

import com.example.musicapp.data.model.SongResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DeezerApiService {

    @GET("artist/{artistId}/top")
    suspend fun getTopSongs(
        @Path("artistId") artistId:String = "13", // son tung
        @Query("limit") limit: Int = 5
    ) : SongResponse
}