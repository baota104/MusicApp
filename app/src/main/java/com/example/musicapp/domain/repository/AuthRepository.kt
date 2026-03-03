package com.example.musicapp.domain.repository

import com.example.musicapp.common.Resource
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    suspend fun signUp(email: String, password: String, username: String): Resource<FirebaseUser>
    suspend fun signIn(email: String, password: String): Resource<FirebaseUser>
    fun isUserLoggedIn(): Boolean
    fun getCurrentUser(): FirebaseUser?
}

