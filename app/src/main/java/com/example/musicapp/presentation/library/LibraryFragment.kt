package com.example.musicapp.presentation.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.MusicApplication
import com.example.musicapp.R
import com.example.musicapp.databinding.FragmentLibraryBinding
import com.example.musicapp.domain.usecase.AddPlaylistUseCase
import com.example.musicapp.domain.usecase.GetPlaylistsUseCase
import com.example.musicapp.presentation.adapter.PlaylistAdapter
import com.example.musicapp.presentation.playlist.PlayListFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.dialog.MaterialDialogs
import kotlinx.coroutines.launch

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!
    private lateinit var libraryViewModel: LibraryViewModel
    private lateinit var playlistAdapter: PlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val appContainer = requireActivity().application as MusicApplication
        val repository = appContainer.songRepository
        val addPlaylistUseCase = AddPlaylistUseCase(repository)
        val getPlaylistsUseCase = GetPlaylistsUseCase(repository)
        val factory = LibraryViewModel.Factory(addPlaylistUseCase, getPlaylistsUseCase)
        libraryViewModel = ViewModelProvider(this, factory).get(LibraryViewModel::class.java)

        setupClickListeners()
        setupRecyclerView()
        setUpObserve()
    }

    private fun setupClickListeners() {
        binding.btnAddPlaylist.setOnClickListener {
            showCreatePlaylistDialog()
        }

        binding.btnFavorite.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, com.example.musicapp.presentation.favorite.FavoriteFragment())
                .addToBackStack(null) // LỆNH NÀY CỰC KỲ QUAN TRỌNG
                .commit()
        }
    }
    private fun showCreatePlaylistDialog() {
        val editText = android.widget.EditText(requireContext()).apply {
            hint = "Enter your playlist name"
            setSingleLine()
            setPadding(50,50,50,50)
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Create new Playlist")
            .setView(editText)
            .setPositiveButton("Create") { dialog, _ ->
                val playlistName = editText.text.toString().trim()
                if (playlistName.isNotEmpty()) {
                    libraryViewModel.addPlaylist(playlistName)
                    Toast.makeText(requireContext(), "Đã tạo: $playlistName", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Tên không được để trống!", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()

    }

    private fun setupRecyclerView() {
        playlistAdapter = PlaylistAdapter {
                clickedPlaylist->
            val bundle = android.os.Bundle().apply {
                putLong("PLAYLIST_ID", clickedPlaylist.id)
                putString("PLAYLIST_NAME", clickedPlaylist.name)
                putString("PLAYLIST_IMAGE_URL", clickedPlaylist.imageUrl)
            }

            val detailFragment = PlayListFragment().apply {
                arguments = bundle
            }

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, detailFragment)
                .addToBackStack(null)
                .commit()
        }
        binding.rvLibrary.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = playlistAdapter
        }
    }
    private fun setUpObserve(){
        viewLifecycleOwner.lifecycleScope.launch {
            libraryViewModel.playlist.collect{
                playlistAdapter.submitList(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}