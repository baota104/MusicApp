package com.example.musicapp.domain.usecase

import com.example.musicapp.common.Resource
import com.example.musicapp.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser

class SignUpUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String, username: String): Resource<FirebaseUser> {
        return repository.signUp(email, password, username)
    }
}