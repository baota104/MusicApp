package com.example.musicapp.domain.usecase

import com.example.musicapp.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser

class GetUserUseCase(
    private val authRepository: AuthRepository
) {
     operator fun invoke(): FirebaseUser? {
        return authRepository.getCurrentUser()
    }

}