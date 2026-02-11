package com.example.musicapp.presentation.home

import android.content.ComponentName
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.R
import com.example.musicapp.common.Resource
import com.example.musicapp.common.toMediaItem
import com.example.musicapp.databinding.FragmentHomeBinding
import com.example.musicapp.domain.model.Song
import com.example.musicapp.presentation.adapter.SongAdapter
import com.example.musicapp.service.MusicService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint // RẤT QUAN TRỌNG: Nếu thiếu dòng này App sẽ crash
class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var songAdapter: SongAdapter

    private var controllerFuture : ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setupRecyclerView()
        observeData()
    }
    override fun onStart() {
        super.onStart()
        initializeController()
    }

    // 2. NGẮT KẾT NỐI KHI THOÁT
    override fun onStop() {
        super.onStop()
        mediaController = null
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
    }

    private fun initializeController() {
        // Tạo Token để trỏ đến MusicService
        val sessionToken = SessionToken(
            requireContext(),
            ComponentName(requireContext(), MusicService::class.java)
        )

        // Tạo Controller bất đồng bộ (Async)
        controllerFuture = MediaController.Builder(requireContext(), sessionToken).buildAsync()
        controllerFuture?.addListener({
            // Khi kết nối thành công, lấy instance controller
            mediaController = controllerFuture?.get()
        }, MoreExecutors.directExecutor())
    }
    // 3. HÀM PHÁT NHẠC (QUAN TRỌNG NHẤT)
    private fun playMusic(selectedSong: Song) {
        val controller = mediaController ?: return // Nếu chưa kết nối xong thì thôi

        // Lấy danh sách bài hát hiện tại từ ViewModel
        val currentList = (viewModel.song.value as? Resource.Success)?.data ?: return

        // Tìm vị trí bài hát được click trong danh sách
        val startIndex = currentList.indexOfFirst { it.id == selectedSong.id }
        if (startIndex == -1) return

        // Chuyển đổi List<Song> -> List<MediaItem>
        val mediaItems = currentList.map { it.toMediaItem() }

        // Gửi lệnh cho Service
        controller.setMediaItems(mediaItems, startIndex, 0) // Nạp danh sách và nhảy tới bài chọn
        controller.prepare() // Chuẩn bị
        controller.play() // Phát nhạc

        Toast.makeText(context, "Đang phát: ${selectedSong.title}", Toast.LENGTH_SHORT).show()
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter { song ->
            playMusic(song)
        }

        binding.rvSongs.apply {
            adapter = songAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeData() {
        // Lắng nghe dữ liệu từ ViewModel
        lifecycleScope.launch {
            viewModel.song.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.tvError.visibility = View.GONE
                        binding.rvSongs.visibility = View.GONE
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.tvError.visibility = View.GONE
                        binding.rvSongs.visibility = View.VISIBLE

                        // Cập nhật dữ liệu vào adapter
                        resource.data?.let { songs ->
                            songAdapter.submitList(songs)
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.rvSongs.visibility = View.GONE
                        binding.tvError.visibility = View.VISIBLE
                        binding.tvError.text = resource.message
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Tránh rò rỉ bộ nhớ
    }
}