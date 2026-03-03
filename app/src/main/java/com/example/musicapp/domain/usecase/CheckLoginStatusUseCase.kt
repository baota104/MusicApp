package com.example.musicapp.domain.usecase

import com.example.musicapp.domain.repository.AuthRepository

class CheckLoginStatusUseCase(private val repository: AuthRepository) {
    operator fun invoke(): Boolean {
        return repository.isUserLoggedIn()
    }
}