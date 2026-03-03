package com.example.musicapp.presentation.playlist_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.musicapp.common.Resource
import com.example.musicapp.domain.model.PlaylistDetail
import com.example.musicapp.domain.model.Song
import com.example.musicapp.domain.usecase.RemoveSongFromPlaylistUseCase
import com.example.musicapp.domain.repository.SongRepository
import com.example.musicapp.domain.usecase.GetFreshSongUseCase
import com.example.musicapp.domain.usecase.GetPlaylistWithSongsUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlaylistDetailViewModel(
    private val getPlayListWithSongsUseCase: GetPlaylistWithSongsUseCase,
    private val removeSongUseCase: RemoveSongFromPlaylistUseCase,
    private val getFreshSongUseCase: GetFreshSongUseCase
) : ViewModel() {

    fun getPlaylistDetail(playlistId: Long): Flow<PlaylistDetail> {
        return getPlayListWithSongsUseCase(playlistId)
    }

    fun removeSong(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            removeSongUseCase(playlistId, songId)
        }
    }
    private val _freshPlaylist = MutableStateFlow<Resource<Pair<Int, List<Song>>>?>(null)
    val freshPlaylist: StateFlow<Resource<Pair<Int, List<Song>>>?> = _freshPlaylist
    private var cachedFreshPlaylist: List<Song>? = null

    fun Playlist(clickedSongId: Long, currentList: List<Song>) {
        viewModelScope.launch {
            _freshPlaylist.value = Resource.Loading()

            try {
                val freshList = if (cachedFreshPlaylist != null && cachedFreshPlaylist!!.size == currentList.size) {
                    android.util.Log.d("CACHE", "Đã dùng lại danh sách từ Cache!")
                    cachedFreshPlaylist!!

                } else {

                    android.util.Log.d("CACHE", "Cache rỗng, bắt đầu gọi API làm mới...")

                    val deferredSongs = currentList.map { song ->
                        async { getFreshSongUseCase(song.id) ?: song }
                    }
                    val fetchedList = deferredSongs.awaitAll()

                    cachedFreshPlaylist = fetchedList
                    fetchedList
                }

                val startIndex = freshList.indexOfFirst { it.id == clickedSongId }
                val finalIndex = if (startIndex != -1) startIndex else 0

                _freshPlaylist.value = Resource.Success(Pair(finalIndex, freshList))

            } catch (e: Exception) {
                _freshPlaylist.value = Resource.Error("Lỗi làm mới danh sách: ${e.message}")
            }
        }
    }

      fun clearCache() {
        cachedFreshPlaylist = null
    }
    fun resetEvent() {
        _freshPlaylist.value = null
    }

    class Factory(
        private val getPlayListWithSongsUseCase: GetPlaylistWithSongsUseCase,
        private val removeSongUseCase: RemoveSongFromPlaylistUseCase,
        private val getFreshSongUseCase: GetFreshSongUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlaylistDetailViewModel(getPlayListWithSongsUseCase, removeSongUseCase,getFreshSongUseCase) as T
        }
    }
}