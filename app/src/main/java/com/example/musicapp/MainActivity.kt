package com.example.musicapp

import android.content.ComponentName
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.bumptech.glide.Glide
import com.example.musicapp.databinding.ActivityMainBinding
import com.example.musicapp.presentation.player.NowPlayingFragment
import com.example.musicapp.service.MusicService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo kết nối tới Service khi mở app
        initializeController()

        setupClickListeners()
    }
    private fun initializeController() {
        val sessionToken = SessionToken(this, ComponentName(this, MusicService::class.java))
        mediaControllerFuture = MediaController.Builder(this, sessionToken).buildAsync()

        mediaControllerFuture?.addListener({
            mediaController = mediaControllerFuture?.get()
            // Khi kết nối thành công, bắt đầu lắng nghe trạng thái nhạc
            mediaController?.addListener(playerListener)

            // Cập nhật UI ngay lập tức nhỡ đâu nhạc đang chạy sẵn từ trước
            updateMiniPlayerUI()
        }, MoreExecutors.directExecutor())
    }

    // Cái tai nghe lén xem ExoPlayer đang làm gì
    private val playerListener = object : Player.Listener {
        // Khi chuyển bài hát mới
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updateMiniPlayerUI()
        }

        // Khi trạng thái Play/Pause thay đổi
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                binding.btnMiniPlayPause.setImageResource(R.drawable.ic_pause) // Cần có icon ic_pause
            } else {
                binding.btnMiniPlayPause.setImageResource(R.drawable.ic_play)
            }
        }
    }

    private fun updateMiniPlayerUI() {
        val player = mediaController ?: return

        // Nếu không có bài hát nào, ẩn Mini Player đi
        if (player.currentMediaItem == null) {
            binding.miniPlayerCard.visibility = View.GONE
            return
        }

        // Nếu có nhạc, hiện nó lên
        binding.miniPlayerCard.visibility = View.VISIBLE

        val metadata = player.currentMediaItem?.mediaMetadata
        binding.tvMiniTitle.text = metadata?.title ?: "Unknown Song"
        binding.tvMiniArtist.text = metadata?.artist ?: "Unknown Artist"

        // Load ảnh bằng Glide
        metadata?.artworkUri?.let { uri ->
            Glide.with(this)
                .load(uri)
                .into(binding.ivMiniCover)
        }

        // Cập nhật trạng thái nút Play/Pause
        if (player.isPlaying) {
            binding.btnMiniPlayPause.setImageResource(R.drawable.ic_pause)
        } else {
            binding.btnMiniPlayPause.setImageResource(R.drawable.ic_play)
        }
    }

    private fun setupClickListeners() {
        binding.btnMiniPlayPause.setOnClickListener {
            mediaController?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                } else {
                    player.play()
                }
            }
        }


        binding.miniPlayerCard.setOnClickListener {
            val nowPlayingFragment = NowPlayingFragment()
            nowPlayingFragment.show(supportFragmentManager, "NowPlayingFragment")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaController?.removeListener(playerListener)
        mediaControllerFuture?.let { MediaController.releaseFuture(it) }
    }
}