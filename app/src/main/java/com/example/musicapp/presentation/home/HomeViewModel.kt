package com.example.musicapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.common.Resource
import com.example.musicapp.domain.model.Song
import com.example.musicapp.domain.usecase.GetTopSongsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel // de hilt biet cach tao viewmodel nay, k can factory de truyen usecase vao
class HomeViewModel @Inject constructor(
    private val getTopSongsUseCase: GetTopSongsUseCase
): ViewModel() {
    private val _song = MutableStateFlow<Resource<List<Song>>>(Resource.Loading())

    // ui nhan list nay
    val song : StateFlow<Resource<List<Song>>> = _song

    init {
        loadSong()
    }
    private fun loadSong(){
        viewModelScope.launch {
            _song.value = Resource.Loading()
           try {
               val result = getTopSongsUseCase()
               _song.value = Resource.Success(result)
               android.util.Log.d("CHECK_API", "Số lượng bài hát lấy được: ${result.size}")
           }catch (e: Exception){
               android.util.Log.e("CHECK_API", "Gặp lỗi rồi: ${e.message}")
               e.printStackTrace()
               _song.value = Resource.Error(e.message ?: "Unknown error")
           }

        }
    }
}