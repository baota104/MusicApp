package com.example.musicapp.presentation.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.R
import com.example.musicapp.databinding.FragmentLibraryBinding

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupRecyclerView()
    }

    private fun setupClickListeners() {
        binding.btnAddPlaylist.setOnClickListener {
            Toast.makeText(requireContext(), "Chuẩn bị làm chức năng Tạo Playlist", Toast.LENGTH_SHORT).show()
        }

        binding.btnFavorite.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, com.example.musicapp.presentation.favorite.FavoriteFragment())
                .addToBackStack(null) // LỆNH NÀY CỰC KỲ QUAN TRỌNG
                .commit()
        }
    }

    private fun setupRecyclerView() {
        binding.rvLibrary.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}