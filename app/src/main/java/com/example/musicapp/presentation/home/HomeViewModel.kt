package com.example.musicapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.musicapp.common.Resource
import com.example.musicapp.domain.model.HomeData
import com.example.musicapp.domain.model.Song
import com.example.musicapp.domain.repository.SongRepository
import com.example.musicapp.domain.usecase.GetHomeDataUseCase
import com.example.musicapp.domain.usecase.GetTopSongsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

//@HiltViewModel // de hilt biet cach tao viewmodel nay, k can factory de truyen usecase vao
class   HomeViewModel (
    private val getTopSongsUseCase: GetTopSongsUseCase,
    private val getHomeDataUseCase: GetHomeDataUseCase
): ViewModel() {

    private val _homeData = MutableStateFlow<Resource<HomeData>>(Resource.Loading())
    val homeData: StateFlow<Resource<HomeData>> = _homeData

    private fun loadHomeData() {
        viewModelScope.launch {
            _homeData.value = Resource.Loading()
            try {
                val result = getHomeDataUseCase()
                _homeData.value = Resource.Success(result)
            }catch (e: Exception){
                _homeData.value = Resource.Error(e.message ?: "Unknown error")
            }
        }
    }

    init {
    loadHomeData()
    }


//    private val _song = MutableStateFlow<Resource<List<Song>>>(Resource.Loading())
//    val song : StateFlow<Resource<List<Song>>> = _song
//
//    private fun loadSong(){
//        viewModelScope.launch {
//            _song.value = Resource.Loading()
//           try {
//               val result = getTopSongsUseCase()
//               _song.value = Resource.Success(result)
//               android.util.Log.d("CHECK_API", "Số lượng bài hát lấy được: ${result.size}")
//           }catch (e: Exception){
//               android.util.Log.e("CHECK_API", "Gặp lỗi rồi: ${e.message}")
//               e.printStackTrace()
//               _song.value = Resource.Error(e.message ?: "Unknown error")
//           }
//
//        }
//    }

    class Factory(
        private val getTopSongsUseCase: GetTopSongsUseCase,
        private val getHomeDataUseCase: GetHomeDataUseCase
    ): ViewModelProvider.Factory{
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {

            return HomeViewModel(getTopSongsUseCase,getHomeDataUseCase) as T
        }
    }
}
