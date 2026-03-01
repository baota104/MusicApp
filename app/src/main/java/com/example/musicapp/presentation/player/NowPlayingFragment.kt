package com.example.musicapp.presentation.player

import android.content.ComponentName
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.musicapp.R
import com.example.musicapp.databinding.FragmentNowPlayingDetailsBinding
import com.example.musicapp.service.MusicService
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class NowPlayingFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentNowPlayingDetailsBinding? = null
    private val binding get() = _binding!!

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    // Quản lý tiến trình cập nhật SeekBar
    private var progressJob: Job? = null
    private var isUserSeeking = false // Kiểm tra xem người dùng có đang giữ tay kéo thanh SeekBar không

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNowPlayingDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Ép BottomSheet mở toàn màn hình thay vì chỉ mở một nửa
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }

        // Cài đặt nút thu nhỏ màn hình (Mũi tên xuống ở XML trước tôi có nhắc)
        // Nếu dùng giao diện mới chưa có nút này, bạn có thể vuốt xuống để đóng cũng được.

        initializeController()
        setupClickListeners()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(
            requireContext(), ComponentName(requireContext(), MusicService::class.java)
        )
        controllerFuture = MediaController.Builder(requireContext(), sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            mediaController?.addListener(playerListener)

            // Lần đầu mở lên, ép UI cập nhật ngay
            updateUI()
        }, MoreExecutors.directExecutor())
    }

    // 2. LẮNG NGHE MỌI ĐỘNG TĨNH TỪ EXOPLAYER
    private val playerListener = object : Player.Listener {
        // Khi chuyển bài mới
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updateUI()
        }

        // Khi người dùng bấm Play/Pause
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updatePlayPauseButton()
            if (isPlaying) {
                startProgressUpdate()
            } else {
                stopProgressUpdate()
            }
        }
        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            updateShuffleButton()
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            updateRepeatButton()
        }
    }

    // 3. CẬP NHẬT GIAO DIỆN CHÍNH (Ảnh, Tên, Ca sĩ, Max Time)
    private fun updateUI() {
        val player = mediaController ?: return
        val metadata = player.currentMediaItem?.mediaMetadata

        // Đổ Text
        binding.tvSongTitle.text = metadata?.title ?: "Unknown"
        binding.tvArtistName.text = metadata?.artist ?: "Unknown"

        // Load ảnh bằng Glide, thêm bo góc luôn nếu ở XML chưa bo
        metadata?.artworkUri?.let { uri ->
            Glide.with(this)
                .load(uri)
                .transform(RoundedCorners(24))
                .into(binding.ivCoverArt)
        }

        // Cập nhật trạng thái nút Play/Pause
        updatePlayPauseButton()

        // Cài đặt độ dài thanh SeekBar (Tổng thời gian)
        val duration = player.duration
        if (duration != -1L) {
            binding.seekBar.max = duration.toInt()
            binding.tvTotalTime.text = formatTime(duration)
        }

        // Cập nhật vị trí hiện tại
        updateProgress()

        // Nếu nhạc đang chạy thì bắt đầu vòng lặp SeekBar
        if (player.isPlaying) {
            startProgressUpdate()
        }

        updateShuffleButton()
        updateRepeatButton()
    }

    private fun updatePlayPauseButton() {
        val isPlaying = mediaController?.isPlaying == true
        // Đảm bảo bạn có 2 icon ic_pause và ic_play_arrow
        val iconRes = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
        binding.btnPlayPause.setImageResource(iconRes)
    }

    // 4. VÒNG LẶP CHẠY SEEKBAR (Dùng Coroutines thay vì Handler)
    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                if (!isUserSeeking) { // Chỉ tự chạy nếu người dùng không cầm vào SeekBar
                    updateProgress()
                }
                delay(1000L) // Chờ 1 giây rồi lặp lại
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
    }

    private fun updateProgress() {
        val player = mediaController ?: return
        val currentPosition = player.currentPosition
        binding.seekBar.progress = currentPosition.toInt()
        binding.tvCurrentTime.text = formatTime(currentPosition)
    }

    // 5. BẮT SỰ KIỆN NÚT BẤM
    private fun setupClickListeners() {
        binding.btnPlayPause.setOnClickListener {
            val player = mediaController ?: return@setOnClickListener
            if (player.isPlaying) player.pause() else player.play()
        }
        binding.btnShuffle.setOnClickListener {
            val player = mediaController ?: return@setOnClickListener
            player.shuffleModeEnabled = !player.shuffleModeEnabled
        }

        // NÚT LẶP LẠI (REPEAT)
        binding.btnRepeat.setOnClickListener {
            val player = mediaController ?: return@setOnClickListener
            player.repeatMode = when (player.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_OFF
                else -> Player.REPEAT_MODE_OFF
            }
        }

        binding.btnNext.setOnClickListener { mediaController?.seekToNext() }
        binding.btnPrevious.setOnClickListener { mediaController?.seekToPrevious() }

        // khong cho tua
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                android.widget.Toast.makeText(requireContext(), "Nhạc Preview 30s không hỗ trợ tua", android.widget.Toast.LENGTH_SHORT).show()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val player = mediaController ?: return
                seekBar?.progress = player.currentPosition.toInt()
            }
        })
    }

    // Tiện ích: Biến Milliseconds thành định dạng Phút:Giây (VD: 2:45)
    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    private fun updateShuffleButton() {
        val isShuffle = mediaController?.shuffleModeEnabled == true
        val color = if (isShuffle) R.color.accent_cyan else R.color.text_secondary

        binding.btnShuffle.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), color)
        )
    }

    private fun updateRepeatButton() {
        val repeatMode = mediaController?.repeatMode ?: Player.REPEAT_MODE_OFF
        when (repeatMode) {
            Player.REPEAT_MODE_OFF -> {
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat)
                binding.btnRepeat.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.text_secondary)
                )
            }
            Player.REPEAT_MODE_ALL -> {
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat)
                binding.btnRepeat.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.accent_cyan)
                )
            }
            Player.REPEAT_MODE_ONE -> {
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat_one)
                binding.btnRepeat.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.accent_cyan)
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Dọn dẹp cẩn thận để chống rỉ RAM
        stopProgressUpdate()
        mediaController?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        _binding = null
    }
}