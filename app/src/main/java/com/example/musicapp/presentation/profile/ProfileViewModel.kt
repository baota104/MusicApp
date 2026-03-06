package com.example.musicapp.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.musicapp.domain.usecase.GetUserUseCase
import com.example.musicapp.domain.usecase.SignOutUseCase
import com.google.firebase.auth.FirebaseUser

class ProfileViewModel(
    private val signOutUseCase: SignOutUseCase,
    private val getUserUseCase: GetUserUseCase

): ViewModel() {
    fun signOut() {
        signOutUseCase()
    }

    fun getUser(): FirebaseUser? {
        val user = getUserUseCase()
        return user
    }
    class Factory(
        private val signOutUseCase: SignOutUseCase,
        private val getUserUseCase: GetUserUseCase
    ): ViewModelProvider.Factory{
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(signOutUseCase, getUserUseCase) as T
        }
    }
}


