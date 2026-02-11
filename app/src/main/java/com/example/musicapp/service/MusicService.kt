package com.example.musicapp.service

import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint // 1. Hilt: Bơm ExoPlayer vào đây
class MusicService : MediaLibraryService() {

    @Inject
    lateinit var player: ExoPlayer // Cái máy phát

    private var mediaLibrarySession: MediaLibrarySession? = null // Cái vô lăng

    // 2. Callback: Nơi xử lý các sự kiện đặc biệt (Optional)
    // Mặc định Media3 đã tự xử lý Play/Pause/Next, nhưng ta cứ khai báo để mở rộng sau này
    private val callback = object : MediaLibrarySession.Callback {
        // Ví dụ: Sau này muốn chặn không cho tua nhạc thì override hàm onSeekTo...
    }

    // 3. Khởi tạo (Lắp ráp máy phát vào vô lăng)
    override fun onCreate() {
        super.onCreate()

        // Kết nối Player vào Session
        mediaLibrarySession = MediaLibrarySession.Builder(this, player, callback)
            .build()
    }

    // 4. Trả về Session khi có ai đó (UI) kết nối tới
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaLibrarySession
    }

    // 5. Dọn dẹp khi Service bị hủy (Rất quan trọng để không bị rò rỉ bộ nhớ)
    override fun onDestroy() {
        mediaLibrarySession?.run {
            player.release() // Giải phóng ExoPlayer
            release() // Giải phóng Session
            mediaLibrarySession = null
        }
        super.onDestroy()
    }
}