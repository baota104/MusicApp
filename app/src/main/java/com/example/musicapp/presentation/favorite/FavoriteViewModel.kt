package com.example.musicapp.presentation.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.musicapp.common.Resource
import com.example.musicapp.domain.model.Song
import com.example.musicapp.domain.usecase.GetFavoriteSongsUseCase
import com.example.musicapp.domain.usecase.GetFreshSongUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoriteViewModel(
    private val getFavoriteSongsUseCase: GetFavoriteSongsUseCase,
    private val getFreshSongUseCase: GetFreshSongUseCase

) : ViewModel() {

    val favoriteSongs: StateFlow<List<Song>> = getFavoriteSongsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    private val _freshPlaylist = MutableStateFlow<Resource<Pair<Int, List<Song>>>?>(null)
    val freshPlaylist: StateFlow<Resource<Pair<Int, List<Song>>>?> = _freshPlaylist
    private var cachedFreshPlaylist: List<Song>? = null

    fun playFavoritePlaylist(clickedSongId: Long, currentList: List<Song>) {
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

    // Thêm hàm này để xóa Cache nếu người dùng xóa/thêm bài hát yêu thích
    fun clearCache() {
        cachedFreshPlaylist = null
    }
    fun resetEvent() {
        _freshPlaylist.value = null
    }

    class Factory(
        private val getFavoriteSongsUseCase: GetFavoriteSongsUseCase,
        private val getFreshSongUseCase: GetFreshSongUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FavoriteViewModel(getFavoriteSongsUseCase,getFreshSongUseCase) as T
        }
    }
}