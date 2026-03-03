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
import com.example.musicapp.domain.usecase.GetUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


class HomeViewModel (
    private val getUserUseCase: GetUserUseCase,
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
    fun getUserName():String {
            val user = getUserUseCase()
            return if (user != null && !user.displayName.isNullOrBlank()) {
                user.displayName!!
            } else {
                "Music Lover"
            }
    }

    init {
    loadHomeData()
    }



    class Factory(
        private val getUserUseCase: GetUserUseCase,
        private val getHomeDataUseCase: GetHomeDataUseCase
    ): ViewModelProvider.Factory{
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {

            return HomeViewModel( getUserUseCase,getHomeDataUseCase) as T
        }
    }
}
