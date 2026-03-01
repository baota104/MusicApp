package com.example.musicapp.data.api

import com.example.musicapp.data.model.AlbumDto
import com.example.musicapp.data.model.DeezerListResponse
import com.example.musicapp.data.model.GenreDto
import com.example.musicapp.data.model.PlaylistDto
import com.example.musicapp.data.model.RadioDto
import com.example.musicapp.data.model.SingleTrackDto
import com.example.musicapp.data.model.SongResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DeezerApiService {

    @GET("artist/{artistId}/top")
    suspend fun getTopSongs(
        @Path("artistId") artistId:String = "13", // enimem
        @Query("limit") limit: Int = 5
    ) : SongResponse

    @GET("chart/0/playlists")
    suspend fun getTopPlaylists(
        @Query("limit") limit: Int = 10
    ): DeezerListResponse<PlaylistDto>

    @GET("radio")
    suspend fun getTopRadios(
        @Query("limit") limit: Int = 10
    ): DeezerListResponse<RadioDto>

    @GET("chart/0/albums")
    suspend fun getTopAlbums(
        @Query("limit") limit: Int = 10
    ): DeezerListResponse<AlbumDto>

    @GET("genre")
    suspend fun getGenres(
        @Query("limit") limit: Int = 10
    ): DeezerListResponse<GenreDto>

    @GET("search")
    suspend fun searchSongs(
        @Query("q") query: String
    ): SongResponse

    @GET("track/{id}")
    suspend fun getTrackById(@Path("id") trackId: Long): SingleTrackDto
}


