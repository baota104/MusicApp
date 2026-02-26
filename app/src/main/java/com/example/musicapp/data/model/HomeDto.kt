package com.example.musicapp.data.model

import com.google.gson.annotations.SerializedName

// 1. KỸ THUẬT GENERIC WRAPPER: Dùng chung cho mọi API trả về danh sách "data"
data class DeezerListResponse<T>(
    @SerializedName("data") val data: List<T>?
)

// 2. Hứng dữ liệu Playlist (Dùng cho "Continue Listening")
data class PlaylistDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String?,
    @SerializedName("picture_medium") val pictureUrl: String? // Chú ý: dùng "picture"
)

// 3. Hứng dữ liệu Radio/Mix (Dùng cho "Your Top Mixes")
data class RadioDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String?,
    @SerializedName("picture_medium") val pictureUrl: String? // Chú ý: dùng "picture"
)

// 4. Hứng dữ liệu Album (Dùng cho "Recent Listening")
data class AlbumDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String?,
    @SerializedName("cover_medium") val coverUrl: String? // Chú ý: Album lại dùng "cover"
)