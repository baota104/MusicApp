package com.example.musicapp.presentation.playlist

import android.content.ComponentName
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.musicapp.MusicApplication
import com.example.musicapp.R
import com.example.musicapp.common.Resource
import com.example.musicapp.common.toMediaItem
import com.example.musicapp.databinding.FragmentPlayListBinding
import com.example.musicapp.domain.model.Playlist
import com.example.musicapp.domain.model.Song
import com.example.musicapp.domain.usecase.GetPlaylistWithSongsUseCase
import com.example.musicapp.domain.usecase.RemoveSongFromPlaylistUseCase
import com.example.musicapp.presentation.adapter.SongAdapter
import com.example.musicapp.presentation.favorite.FavoriteViewModel
import com.example.musicapp.presentation.playlist_detail.PlaylistDetailViewModel
import com.example.musicapp.service.MusicService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.launch

class PlayListFragment : Fragment() {
    private var _binding: FragmentPlayListBinding? = null
    private val binding get() = _binding!!
    private lateinit var PlayListViewModel: PlaylistDetailViewModel
    private lateinit var songAdapter: SongAdapter
    private var currentList: List<Song> = emptyList()
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var playlistId: Long = 0L
    private var playlistName: String = ""
    private var playlistImageUrl: String = ""

    private val args: PlayListFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlayListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        arguments?.let {
//            playlistId = it.getLong("PLAYLIST_ID", -1L)
//            playlistName = it.getString("PLAYLIST_NAME", "Playlist")
//            playlistimage = it.getString("PLAYLIST_IMAGE_URL", "")
//
//        }
        playlistId = args.playlistId
        playlistName = args.playlistName
        playlistImageUrl = args.playlistImageUrl
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val appContainer = requireActivity().application as MusicApplication
        val repository = appContainer.songRepository
        val getPlayListWithSongsUseCase = GetPlaylistWithSongsUseCase(repository)
        val removeSongUseCase = RemoveSongFromPlaylistUseCase(repository)
        val freshSongUseCase = com.example.musicapp.domain.usecase.GetFreshSongUseCase(repository)
        val factory =
            PlaylistDetailViewModel.Factory(getPlayListWithSongsUseCase, removeSongUseCase, freshSongUseCase)
        PlayListViewModel =
            androidx.lifecycle.ViewModelProvider(this, factory)[PlaylistDetailViewModel::class.java]

        Log.d("PlayListFragment", "playlistId: $playlistId, playlistName: $playlistName")
        setUpUi()
        setUpClickListener()
        setUpRecycle()
        setupObserve()
    }

    private fun setUpUi() {
        binding.tvPlaylistName.text = playlistName
        Glide.with(binding.root.context)
            .load(playlistImageUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(binding.coverplaylist)

    }

    private fun setUpClickListener() {
        binding.btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setUpRecycle() {
        songAdapter = SongAdapter { onclick ->
            PlayListViewModel.Playlist(onclick.id, currentList = currentList)
        }
        binding.rvPlaylistDetail.apply {
            adapter = songAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        }
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(
            0, // Không hỗ trợ kéo thả lên xuống (Drag)
            ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                r: RecyclerView,
                v: RecyclerView.ViewHolder,
                t: RecyclerView.ViewHolder
            ) = false

            // 1. Khi vuốt hoàn thành (Vượt quá ngưỡng)
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val songToDelete = songAdapter.currentList[position]

                // GỌI VIEWMODEL ĐỂ XÓA KHỎI DATABASE
                PlayListViewModel.removeSong(playlistId, songToDelete.id)
                android.widget.Toast.makeText(
                    requireContext(),
                    "Đã xóa ${songToDelete.title}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // 2. Vẽ nền đỏ và Icon Thùng rác khi đang vuốt
            override fun onChildDraw(
                c: android.graphics.Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val deleteIcon = androidx.core.content.ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_delete
                )
                val background =
                    ColorDrawable(Color.parseColor("#f44336"))

                // Tính toán vị trí vẽ
                val iconMargin = (itemView.height - deleteIcon!!.intrinsicHeight) / 2
                val iconTop = itemView.top + (itemView.height - deleteIcon.intrinsicHeight) / 2
                val iconBottom = iconTop + deleteIcon.intrinsicHeight

                if (dX < 0) { // Đang vuốt sang trái
                    val iconLeft = itemView.right - iconMargin - deleteIcon.intrinsicWidth
                    val iconRight = itemView.right - iconMargin
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    background.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                } else { // Không vuốt
                    background.setBounds(0, 0, 0, 0)
                }

                // Vẽ nền đỏ trước, vẽ icon lên trên
                background.draw(c)
                deleteIcon.draw(c)

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.rvPlaylistDetail)
    }

    private fun setupObserve() {

        viewLifecycleOwner.lifecycleScope.launch {
            PlayListViewModel.getPlaylistDetail(playlistId).collect { detail ->

                if (currentList.size != detail.songs.size && currentList.isNotEmpty()) {
                    PlayListViewModel.clearCache()
                }
                songAdapter.submitList(detail.songs)
                currentList = detail.songs
                Log.d("PlayListFragment", "Songs in playlist: ${detail.songs}")

            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            PlayListViewModel.freshPlaylist.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val pairData = resource.data
                        if (pairData != null) {
                            val startIndex = pairData.first
                            val freshList = pairData.second
                            playMusic(startIndex, freshList)
                            PlayListViewModel.resetEvent()

                        }
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                        PlayListViewModel.resetEvent()
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

//    override fun onResume() {
//        super.onResume()
//        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_navigation)
//        bottomNav?.visibility = View.GONE
//    }

    override fun onDestroyView() {
        super.onDestroyView()
//        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_navigation)
//        bottomNav?.visibility = View.VISIBLE
        PlayListViewModel.clearCache()
        _binding = null
    }
}

