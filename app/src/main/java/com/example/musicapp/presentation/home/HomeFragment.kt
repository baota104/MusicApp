package com.example.musicapp.presentation.home

import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.MusicApplication
import com.example.musicapp.R
import com.example.musicapp.common.Resource
import com.example.musicapp.common.toMediaItem
import com.example.musicapp.databinding.FragmentHomeBinding
import com.example.musicapp.domain.model.Song
import com.example.musicapp.domain.usecase.GetHomeDataUseCase
import com.example.musicapp.domain.usecase.GetTopSongsUseCase
import com.example.musicapp.domain.usecase.GetUserUseCase
import com.example.musicapp.presentation.adapter.RecentListeningAdapter
import com.example.musicapp.presentation.adapter.SongAdapter
import com.example.musicapp.presentation.adapter.TopMixsAdapter
import com.example.musicapp.presentation.home.adapter.ContinueListeningAdapter
import com.example.musicapp.service.MusicService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel
    private lateinit var continueAdapter: ContinueListeningAdapter
    private lateinit var recentListeningAdapter: RecentListeningAdapter
    private lateinit var topMixsAdapter: TopMixsAdapter
    private lateinit var songAdapter: SongAdapter
    private lateinit var currentlist : List<Song>
    private var controllerFuture : ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)
        val appContainer = requireActivity().application as MusicApplication
        val repository = appContainer.songRepository
        val getHomeDataUseCase = GetHomeDataUseCase(repository)
        val getUserUseCase: GetUserUseCase = GetUserUseCase(appContainer.authenticationRepository)
        val factory = HomeViewModel.Factory(getUserUseCase, getHomeDataUseCase)
        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

        setupGreeting()
        setupRecyclerView()
        observeData()

    }
    private fun setupGreeting() {
        val userName = viewModel.getUserName()
        binding.tvUserName.text = userName

        binding.ivSettings.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
        }
    }

    private fun setupRecyclerView() {
        continueAdapter = ContinueListeningAdapter { clickedItem ->
            Toast.makeText(requireContext(), "Clicked: ${clickedItem.title}", Toast.LENGTH_SHORT).show()
        }

        binding.rvContinueListening.apply {
            adapter = continueAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
//            setHasFixedSize(true)
        }

        topMixsAdapter = TopMixsAdapter{
            clickedItem ->
            Toast.makeText(requireContext(), "Clicked: ${clickedItem.title}", Toast.LENGTH_SHORT).show()
        }
        binding.rvTopMixes.apply {
            adapter = topMixsAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        }
        recentListeningAdapter = RecentListeningAdapter {
            clickedItem ->
            playMusic(clickedItem)
            Toast.makeText(requireContext(), "Clicked: ${clickedItem.title}", Toast.LENGTH_SHORT).show()
        }
        binding.rvRecent.apply {
            adapter = recentListeningAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }


    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.homeData.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {

                    }
                    is Resource.Success -> {

                       resource.data?.let { data ->
                            Log.d("DEBUG_UI", "Số bài hát Continue: ${data.continueListening.size}")
                           continueAdapter.submitList(data.continueListening)
                           topMixsAdapter.submitList(data.topMixes)
                           recentListeningAdapter.submitList(data.playlists)
                           currentlist = data.playlists

                       }
//
                    }
                    is Resource.Error -> {

                        Log.e("CHECK_API", "Error: ${resource.message}")

                    }
                }
            }
        }
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
        val currentList = currentlist ?: return

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Tránh rò rỉ bộ nhớ
    }
}