package com.example.musicapp.common

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.example.musicapp.domain.model.Song

// Hàm mở rộng (Extension function) để chuyển Song thành MediaItem
fun Song.toMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setUri(this.sourceUrl) // Đường dẫn file nhạc (Preview URL từ Deezer)
        .setMediaId(this.id.toString())
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(this.title)
                .setArtist(this.artistName)
                .setArtworkUri(android.net.Uri.parse(this.coverUrl)) // Ảnh bìa
                .build()
        )
        .build()
}