package com.example.musicapp.data.model

import com.google.gson.annotations.SerializedName

// 1. KỸ THUẬT GENERIC WRAPPER: Dùng chung cho mọi API trả về danh sách "data"
data class DeezerListResponse<T>(
    @SerializedName("data") val data: List<T>?
)

data class PlaylistDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String?,
    @SerializedName("picture_medium") val pictureUrl: String? // Chú ý: dùng "picture"
)

data class RadioDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String?,
    @SerializedName("picture_medium") val pictureUrl: String? // Chú ý: dùng "picture"
)

data class AlbumDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String?,
    @SerializedName("cover_medium") val coverUrl: String? // Chú ý: Album lại dùng "cover"
)

data class GenreDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String?, // Lưu ý: API này dùng "name" thay vì "title"
    @SerializedName("picture_medium") val pictureUrl: String?
)


data class SingleTrackDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("preview") val previewUrl: String?, // Link nhạc tươi mới!
    @SerializedName("album") val album: AlbumDto?,
    @SerializedName("artist") val artist: ArtistDto?,
    @SerializedName("duration") val duration: Int
)