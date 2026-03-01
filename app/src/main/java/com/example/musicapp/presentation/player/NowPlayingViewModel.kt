package com.example.musicapp.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.musicapp.domain.model.Song
import com.example.musicapp.domain.usecase.CheckFavoriteUseCase
import com.example.musicapp.domain.usecase.GetFreshSongUseCase
import com.example.musicapp.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NowPlayingViewModel(
    private val checkFavoriteUseCase: CheckFavoriteUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
) : ViewModel() {

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite

    fun checkIsFavorite(songId: Long) {
        viewModelScope.launch {
            _isFavorite.value = checkFavoriteUseCase(songId)
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            val newState = toggleFavoriteUseCase(song)
            _isFavorite.value = newState
        }
    }

    class Factory(
        private val checkFavoriteUseCase: CheckFavoriteUseCase,
        private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NowPlayingViewModel(checkFavoriteUseCase, toggleFavoriteUseCase) as T
        }
    }
}