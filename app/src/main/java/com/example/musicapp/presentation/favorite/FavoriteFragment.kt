package com.example.musicapp.presentation.favorite

import android.content.ComponentName
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.MusicApplication
import com.example.musicapp.common.Resource
import com.example.musicapp.common.toMediaItem
import com.example.musicapp.databinding.FragmentFavoriteBinding
import com.example.musicapp.domain.model.Song
import com.example.musicapp.domain.usecase.GetFavoriteSongsUseCase
import com.example.musicapp.domain.usecase.GetFreshSongUseCase
import com.example.musicapp.presentation.adapter.SongAdapter
import com.example.musicapp.service.MusicService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.launch

class FavoriteFragment : Fragment() {
   private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    private lateinit var favoriteViewModel: FavoriteViewModel
    private lateinit var songAdapter: SongAdapter
    private lateinit var currentList: List<Song>
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val appContainer = requireActivity().application as MusicApplication
        val repository = appContainer.songRepository
        val getFavoriteSongsUseCase = GetFavoriteSongsUseCase(repository)
        val freshSongUseCase = GetFreshSongUseCase(repository)
        val factory = FavoriteViewModel.Factory(getFavoriteSongsUseCase, freshSongUseCase)
        favoriteViewModel = ViewModelProvider(this, factory)[FavoriteViewModel::class.java]
        setUpRecycleView()
        setupClickListeners()
        setupObserve()

    }

    private fun setUpRecycleView(){
        songAdapter = SongAdapter { onclick ->
            favoriteViewModel.playFavoritePlaylist(onclick.id,currentList)
        }
        binding.rvFavoriteSongs.apply {
            adapter = songAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
    }
    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
    private fun setupObserve() {
        viewLifecycleOwner.lifecycleScope.launch {
            favoriteViewModel.favoriteSongs.collect { songs ->
                songAdapter.submitList(songs)
                currentList = songs
                favoriteViewModel.clearCache()
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            favoriteViewModel.freshPlaylist.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val pairData = resource.data
                        if (pairData != null) {
                            val startIndex = pairData.first
                            val freshList = pairData.second
                            playMusic(startIndex, freshList)
                            favoriteViewModel.resetEvent()

                        }
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                        favoriteViewModel.resetEvent()
                    }
                    is Resource.Loading -> { } // Có thể hiện xoay xoay ở đây
                    null -> {}
                }
            }
        }

    }

    override fun onStart() {
        super.onStart()
        initializeController()
    }

    override fun onStop() {
        super.onStop()
        mediaController = null
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
    }

    private fun initializeController() {
        val sessionToken = SessionToken(
            requireContext(),
            ComponentName(requireContext(), MusicService::class.java)
        )

        controllerFuture = MediaController.Builder(requireContext(), sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
        }, MoreExecutors.directExecutor())
    }
    private fun playMusic(startIndex: Int, freshList: List<Song>) {
        val controller = mediaController ?: return

        val mediaItems = freshList.map { song ->
            song.toMediaItem(isFromDatabase = false)
        }

        controller.setMediaItems(mediaItems, startIndex, 0)
        controller.prepare()
        controller.play()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        favoriteViewModel.clearCache()
        _binding = null
    }



}