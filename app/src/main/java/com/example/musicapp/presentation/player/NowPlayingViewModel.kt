package com.example.musicapp.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.musicapp.domain.model.Playlist
import com.example.musicapp.domain.model.Song
import com.example.musicapp.domain.usecase.AddPlaylistUseCase
import com.example.musicapp.domain.usecase.AddSongToPlaylistUseCase
import com.example.musicapp.domain.usecase.CheckFavoriteUseCase
import com.example.musicapp.domain.usecase.GetFreshSongUseCase
import com.example.musicapp.domain.usecase.GetPlaylistsUseCase
import com.example.musicapp.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NowPlayingViewModel(
    private val checkFavoriteUseCase: CheckFavoriteUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val addSongToPlaylistUseCase: AddSongToPlaylistUseCase,
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val addPlaylistUseCase: AddPlaylistUseCase,
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
            val newState = !_isFavorite.value
            toggleFavoriteUseCase(song, newState)
            _isFavorite.value = newState
        }
    }
    val playlists: StateFlow<List<Playlist>> = getPlaylistsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    fun createPlaylist(name: String) {
        viewModelScope.launch {
            addPlaylistUseCase(name)
        }
    }

    fun addSongToPlaylist(playlistId: Long, song:Song) {
        viewModelScope.launch {
            addSongToPlaylistUseCase(playlistId, song)
        }
    }

    class Factory(
        private val checkFavoriteUseCase: CheckFavoriteUseCase,
        private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
        private val addSongToPlaylistUseCase: AddSongToPlaylistUseCase,
        private val getPlaylistsUseCase: GetPlaylistsUseCase,
        private val addPlaylistUseCase: AddPlaylistUseCase,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NowPlayingViewModel(checkFavoriteUseCase, toggleFavoriteUseCase, addSongToPlaylistUseCase, getPlaylistsUseCase, addPlaylistUseCase) as T
        }
    }
}