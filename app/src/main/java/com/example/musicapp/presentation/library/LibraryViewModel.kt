package com.example.musicapp.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.musicapp.domain.model.Playlist
import com.example.musicapp.domain.usecase.AddPlaylistUseCase
import com.example.musicapp.domain.usecase.GetPlaylistsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val addPlaylistUseCase: AddPlaylistUseCase,
    private val getPlaylistsUseCase: GetPlaylistsUseCase
): ViewModel() {
    val playlist : StateFlow<List<Playlist>> = getPlaylistsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    fun addPlaylist(name: String) {
        viewModelScope.launch {
            addPlaylistUseCase(name)
        }
    }

    class Factory(
        private val addPlaylistUseCase: AddPlaylistUseCase,
        private val getPlaylistsUseCase: GetPlaylistsUseCase
    ): ViewModelProvider.Factory{
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {

            return LibraryViewModel(addPlaylistUseCase,getPlaylistsUseCase) as T
        }
    }

}