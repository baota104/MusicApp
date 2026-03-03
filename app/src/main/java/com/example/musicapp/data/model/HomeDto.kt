package com.example.musicapp.data.model

import com.google.gson.annotations.SerializedName

data class DeezerListResponse<T>(
    @SerializedName("data") val data: List<T>?
)

data class PlaylistDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String?,
    @SerializedName("picture_medium") val pictureUrl: String?
)

data class RadioDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String?,
    @SerializedName("picture_medium") val pictureUrl: String?
)

data class AlbumDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String?,
    @SerializedName("cover_medium") val coverUrl: String?
)

data class GenreDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String?,
    @SerializedName("picture_medium") val pictureUrl: String?
)


data class SingleTrackDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("preview") val previewUrl: String?,
    @SerializedName("album") val album: AlbumDto?,
    @SerializedName("artist") val artist: ArtistDto?,
    @SerializedName("duration") val duration: Int
)