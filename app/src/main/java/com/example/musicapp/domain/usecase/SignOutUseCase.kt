package com.example.musicapp.domain.usecase

import com.example.musicapp.domain.repository.AuthRepository

class SignOutUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(){
        authRepository.signOut()
    }
}