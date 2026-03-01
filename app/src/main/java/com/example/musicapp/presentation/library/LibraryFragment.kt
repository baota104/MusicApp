package com.example.musicapp.presentation.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
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
        // Xử lý khi bấm nút Tạo Playlist mới
        binding.btnAddPlaylist.setOnClickListener {
            // Tương lai: Sẽ hiện lên một Dialog (Hộp thoại) để nhập tên Playlist
            Toast.makeText(requireContext(), "Chuẩn bị làm chức năng Tạo Playlist", Toast.LENGTH_SHORT).show()
        }

        // Xử lý khi bấm nút Bài hát yêu thích
        binding.btnFavorite.setOnClickListener {
            // Tương lai: Mở ra một màn hình chứa danh sách bài hát đã Thả tim
            Toast.makeText(requireContext(), "Chuẩn bị mở danh sách Yêu thích", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        // Tương lai: Nơi đây sẽ khởi tạo Adapter để hiển thị danh sách Playlist từ Room DB
        binding.rvLibrary.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}