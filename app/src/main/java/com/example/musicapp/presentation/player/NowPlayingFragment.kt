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
import com.example.musicapp.domain.usecase.AddPlaylistUseCase
import com.example.musicapp.domain.usecase.AddSongToPlaylistUseCase
import com.example.musicapp.domain.usecase.CheckFavoriteUseCase
import com.example.musicapp.domain.usecase.GetPlaylistsUseCase
import com.example.musicapp.domain.usecase.ToggleFavoriteUseCase
import com.example.musicapp.presentation.adapter.PlaylistAdapter
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

    //seek bar
    private var progressJob: Job? = null
    private var isUserSeeking = false // Kiểm tra xem người dùng có đang giữ tay kéo thanh SeekBar không

    private lateinit var viewModel: NowPlayingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNowPlayingDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
        val appContainer = requireActivity().application as com.example.musicapp.MusicApplication
        val repository = appContainer.songRepository
        val checkFav = CheckFavoriteUseCase(repository)
        val toggleFav = ToggleFavoriteUseCase(repository)
        val addSongToPlaylist = AddSongToPlaylistUseCase(repository)
        val getPlaylists = GetPlaylistsUseCase(repository)
        val addPlaylist = AddPlaylistUseCase(repository)
        val factory = NowPlayingViewModel.Factory(checkFav, toggleFav,addSongToPlaylist, getPlaylists, addPlaylist)
        viewModel = androidx.lifecycle.ViewModelProvider(this, factory)[NowPlayingViewModel::class.java]

        initializeController()
        setupClickListeners()
        setupObserve()
    }

    private fun setupObserve() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isFavorite.collect { isFav ->
                if (isFav) {
                    binding.ivFavorite.setImageResource(R.drawable.ic_heart)
                    binding.ivFavorite.setColorFilter(ContextCompat.getColor(requireContext(), R.color.accent_green))
                } else {
                    binding.ivFavorite.setImageResource(R.drawable.ic_heart)
                    binding.ivFavorite.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
                }
            }
        }
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

    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updateUI()
        }

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

    private fun updateUI() {
        val player = mediaController ?: return
        val metadata = player.currentMediaItem?.mediaMetadata

        binding.tvSongTitle.text = metadata?.title ?: "Unknown"
        binding.tvArtistName.text = metadata?.artist ?: "Unknown"

        metadata?.artworkUri?.let { uri ->
            Glide.with(this)
                .load(uri)
                .transform(RoundedCorners(24))
                .into(binding.ivCoverArt)
        }

        updatePlayPauseButton()

        val duration = player.duration
        if (duration != -1L) {
            binding.seekBar.max = duration.toInt()
            binding.tvTotalTime.text = formatTime(duration)
        }

        updateProgress()

        if (player.isPlaying) {
            startProgressUpdate()
        }

        updateShuffleButton()
        updateRepeatButton()
        val songId = player.currentMediaItem?.mediaId?.toLongOrNull() ?: 0L
        viewModel.checkIsFavorite(songId)
    }


    private fun updatePlayPauseButton() {
        val isPlaying = mediaController?.isPlaying == true
        val iconRes = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
        binding.btnPlayPause.setImageResource(iconRes)
    }

    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                if (!isUserSeeking) {
                    updateProgress()
                }
                delay(1000L)
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

    private fun setupClickListeners() {
        binding.btnadd.setOnClickListener {
            showAddToPlaylistDialog()
        }
        binding.btnPlayPause.setOnClickListener {
            val player = mediaController ?: return@setOnClickListener
            if (player.isPlaying) player.pause() else player.play()
        }
        binding.btnShuffle.setOnClickListener {
            val player = mediaController ?: return@setOnClickListener
            player.shuffleModeEnabled = !player.shuffleModeEnabled
        }

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

        binding.ivFavorite.setOnClickListener {
            val player = mediaController ?: return@setOnClickListener
            val currentItem = player.currentMediaItem ?: return@setOnClickListener

            val song = com.example.musicapp.domain.model.Song(
                id = currentItem.mediaId.toLongOrNull() ?: 0L,
                title = currentItem.mediaMetadata.title.toString(),
                artistName = currentItem.mediaMetadata.artist.toString(),
                coverUrl = currentItem.mediaMetadata.artworkUri?.toString() ?: "",
                sourceUrl = currentItem.localConfiguration?.uri?.toString() ?: "",
                duration = (player.duration / 1000).toInt()
            )
            viewModel.toggleFavorite(song)
        }

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

    private fun showAddToPlaylistDialog() {
        val player = mediaController ?: return
        val currentItem = player.currentMediaItem ?: return
        val currentSong = com.example.musicapp.domain.model.Song(
            id = currentItem.mediaId.toLongOrNull() ?: 0L,
            title = currentItem.mediaMetadata.title?.toString() ?: "Unknown",
            artistName = currentItem.mediaMetadata.artist?.toString() ?: "Unknown",
            coverUrl = currentItem.mediaMetadata.artworkUri?.toString() ?: "",
            sourceUrl = currentItem.localConfiguration?.uri?.toString() ?: "",
            // Ép kiểu duration cẩn thận, đề phòng trường hợp player chưa tải xong thời lượng
            duration = if (player.duration > 0) (player.duration / 1000).toInt() else 0
        )


        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_add_to_playlist, null)
        dialog.setContentView(view)

        val btnCreateNew = view.findViewById<View>(R.id.btnCreateNewPlaylist)
        val rvPlaylists = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvPlaylistsDialog)

        btnCreateNew.setOnClickListener {
            dialog.dismiss()
            showCreatePlaylistInputDialog()
        }
        val adapter = PlaylistAdapter { clickedPlaylist ->
            viewModel.addSongToPlaylist(clickedPlaylist.id, currentSong)
            android.widget.Toast.makeText(requireContext(), "Added to ${clickedPlaylist.name}", android.widget.Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        rvPlaylists.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        rvPlaylists.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.playlists.collect { list ->
                adapter.submitList(list)
            }
        }

        dialog.show()
    }

    private fun showCreatePlaylistInputDialog() {
        val editText = android.widget.EditText(requireContext()).apply {
            hint = "Enter your playlist name"
            setSingleLine()
            setPadding(50,50,50,50)
        }
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("Create new Playlist")
            .setView(editText)
            .setPositiveButton("Create") { dialog, _ ->
                val playlistName = editText.text.toString().trim()
                if (playlistName.isNotEmpty()) {
                    viewModel.createPlaylist(playlistName) // Lưu vào DB
                    android.widget.Toast.makeText(requireContext(), "Created: $playlistName", android.widget.Toast.LENGTH_SHORT).show()
                    showAddToPlaylistDialog()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        stopProgressUpdate()
        mediaController?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        _binding = null
    }
}