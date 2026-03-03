package com.example.musicapp.presentation.search

import android.content.ComponentName
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.MusicApplication
import com.example.musicapp.R
import com.example.musicapp.common.Resource
import com.example.musicapp.common.toMediaItem
import com.example.musicapp.databinding.FragmentSearchBinding
import com.example.musicapp.domain.model.Song
import com.example.musicapp.domain.usecase.GetGenreUseCase
import com.example.musicapp.domain.usecase.SearchSongsUseCase
import com.example.musicapp.presentation.adapter.SongAdapter
import com.example.musicapp.presentation.search.adapter.GenreAdapter
import com.example.musicapp.service.MusicService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var topGenresAdapter: GenreAdapter
    private lateinit var browseAllAdapter: GenreAdapter
    private lateinit var songAdapter: SongAdapter
    private var controllerFuture : ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var currentlist = emptyList<Song>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appContainer = requireActivity().application as MusicApplication
        val repository = appContainer.songRepository
        val getGenresUseCase = GetGenreUseCase(repository)
        val searchSongsUseCase = SearchSongsUseCase(repository)
        val factory = SearchViewModel.Factory(getGenresUseCase,searchSongsUseCase)
        searchViewModel = ViewModelProvider(this, factory)[SearchViewModel::class.java]

        setupRecycleview()
        setUpInput()
        setupObserve()
    }
    private fun setUpInput(){
        binding.etSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                searchViewModel.search(query)
                if (query.isNotEmpty()) {
                    binding.tvTopGenres.visibility = View.GONE
                    binding.rvTopGenres.visibility = View.GONE
                    binding.tvBrowseAll.visibility = View.GONE
                    binding.rvBrowseAll.visibility = View.GONE
                    binding.rvSearchResults.visibility = View.VISIBLE
                } else {
                    binding.tvTopGenres.visibility = View.VISIBLE
                    binding.rvTopGenres.visibility = View.VISIBLE
                    binding.tvBrowseAll.visibility = View.VISIBLE
                    binding.rvBrowseAll.visibility = View.VISIBLE
                    binding.rvSearchResults.visibility = View.GONE
                }
            }
        })
    }

    private fun setupRecycleview() {

        topGenresAdapter = GenreAdapter { onclick ->
            Toast.makeText(requireContext(), "Clicked: ${onclick.name}", Toast.LENGTH_SHORT).show()
        }
        binding.rvTopGenres.apply {
            adapter = topGenresAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
        browseAllAdapter = GenreAdapter { onclick ->
            Toast.makeText(requireContext(), "Clicked: ${onclick.name}", Toast.LENGTH_SHORT).show()
        }
        binding.rvBrowseAll.apply {
            adapter = browseAllAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
        songAdapter = SongAdapter{
            onclick ->
            playMusic( onclick)
        }
        binding.rvSearchResults.apply {
            adapter = songAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }

    }

    private fun setupObserve(){
        viewLifecycleOwner.lifecycleScope.launch {
            searchViewModel.genres.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {

                    }
                    is Resource.Success -> {

                        resource.data?.let { data ->
                           val topGenresList = data.take(4)
                            val browseAllList = data.drop(4)
                            topGenresAdapter.submitList(topGenresList)
                            browseAllAdapter.submitList(browseAllList)
                        }
                    }
                    is Resource.Error -> {

                        Log.e("CHECK_API", "Error: ${resource.message}")

                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            searchViewModel.searchResults.collect { resource ->
                when (resource) {
                    is Resource.Loading -> { }
                    is Resource.Success -> {
                        resource.data?.let { songs ->
                            songAdapter.submitList(songs)
                            currentlist = songs
                        }
                    }
                    is Resource.Error -> {
                        Log.e("CHECK_API", "Search Error: ${resource.message}")
                    }
                }
            }
        }

    }

    private fun initializeController() {
        val sessionToken = SessionToken(
            requireContext(),
            ComponentName(requireContext(), MusicService::class.java)
        )
        controllerFuture = MediaController.Builder(requireContext(),sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
        }, MoreExecutors.directExecutor())

    }

    private fun playMusic(selectedSong: Song) {
        val controller = mediaController ?: return

        val startIndex = currentlist.indexOfFirst { it.id == selectedSong.id }
        if (startIndex == -1) return
        val mediaItems = currentlist.map { it.toMediaItem() }

        controller.setMediaItems(mediaItems, startIndex, 0)
        controller.prepare()
        controller.play()

        Toast.makeText(context, "Đang phát: ${selectedSong.title}", Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }

}