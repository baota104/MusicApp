package com.example.musicapp.data.model

import com.google.gson.annotations.SerializedName

// dai dien cho 1 cuc json tra ve
data class SongResponse(
    @SerializedName("data")
    val data: List<SongDto>, // danh sach bai hat
    @SerializedName("total")
    val total: Int // tong so bai hat
)
// dai dien cho tung bai hat trong danh sach
data class SongDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("title")
    val title: String,

    @SerializedName("preview")
    val previewUrl: String, // Link nhạc 30s để play

    @SerializedName("duration")
    val duration: Int,

    @SerializedName("artist")
    val artist: ArtistDto,

    @SerializedName("album")
    val album: AlbumDto
)

data class ArtistDto(
    @SerializedName("name")
    val name: String
)

data class AlbumDto(
    @SerializedName("cover_medium")
    val coverUrl: String,

    @SerializedName("title")
    val title: String
)
