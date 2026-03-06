package com.example.musicapp.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.musicapp.common.Resource
import com.example.musicapp.domain.model.Genre
import com.example.musicapp.domain.model.Song
import com.example.musicapp.domain.usecase.GetGenreUseCase
import com.example.musicapp.domain.usecase.SearchSongsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val getGenresUseCase: GetGenreUseCase,
    private val searchSongsUseCase : SearchSongsUseCase
): ViewModel() {
    private val _genres = MutableStateFlow<Resource<List<Genre>>>(Resource.Loading())
    val genres: StateFlow<Resource<List<Genre>>> = _genres
    private val _searchResults = MutableStateFlow<Resource<List<Song>>>(Resource.Success(emptyList()))
    val searchResults: StateFlow<Resource<List<Song>>> = _searchResults
    private var searchJob: Job? = null

    init {
    loadGenres()
    }

    fun loadGenres() {
        viewModelScope.launch {
            _genres.value = Resource.Loading()
            try {
                val result = getGenresUseCase()
                _genres.value = Resource.Success(result)
                android.util.Log.d("CHECK_API", "Số lượng thể loại: ${result.size}")
            } catch (e: Exception) {
                _genres.value = Resource.Error(e.message ?: "Unknown error")
            }
        }
    }
    fun search(query: String) {
        // 1. Hủy ngay lập tức cái tìm kiếm cũ nếu người dùng vẫn đang gõ chữ
        searchJob?.cancel()

        // Nếu xóa hết chữ, trả về danh sách rỗng ngay lập tức
        if (query.trim().isEmpty()) {
            _searchResults.value = Resource.Success(emptyList())
            return
        }

        // 2. Tạo một tiến trình tìm kiếm mới
        searchJob = viewModelScope.launch {
            delay(500L) // Bấm đồng hồ chờ 500ms (Nửa giây)

            // Nếu qua 500ms mà người dùng không gõ thêm chữ nào -> Tiến hành gọi API
            _searchResults.value = Resource.Loading()
            try {
                val results = searchSongsUseCase(query)
                _searchResults.value = Resource.Success(results)
            } catch (e: Exception) {
                _searchResults.value = Resource.Error(e.message ?: "Lỗi tìm kiếm")
            }
        }
    }
    class Factory(
        private val getGenresUseCase: GetGenreUseCase,
        private val searchSongsUseCase: SearchSongsUseCase
    ): ViewModelProvider.Factory{
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(getGenresUseCase, searchSongsUseCase) as T

        }
    }

}




