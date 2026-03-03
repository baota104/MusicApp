package com.example.musicapp.domain.usecase

import com.example.musicapp.common.Resource
import com.example.musicapp.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Resource<FirebaseUser> {
        return repository.signIn(email, password)
    }
}