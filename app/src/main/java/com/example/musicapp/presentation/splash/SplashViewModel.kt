package com.example.musicapp.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.musicapp.domain.usecase.CheckLoginStatusUseCase

class SplashViewModel(
    private val checkLoginStatusUseCase: CheckLoginStatusUseCase
) : ViewModel(){

    fun isUserLoggedIn(): Boolean {
        return checkLoginStatusUseCase()
    }
    class Factory(
        private val checkLoginStatusUseCase: CheckLoginStatusUseCase

    ): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SplashViewModel(checkLoginStatusUseCase) as T
        }
    }
}