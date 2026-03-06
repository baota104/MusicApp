package com.example.musicapp.data.repository

import com.example.musicapp.common.Resource
import com.example.musicapp.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val auth: FirebaseAuth // Đã sửa từ authen thành auth cho đồng nhất
) : AuthRepository {

    override suspend fun signUp(email: String, password: String, username: String): Resource<FirebaseUser> {
        return try {

            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user

            if (user != null) {
                val profileUpdates = userProfileChangeRequest {
                    displayName = username
                }
                user.updateProfile(profileUpdates).await()
                Resource.Success(user)
            } else {
                Resource.Error<FirebaseUser>("Lỗi không xác định: Không lấy được thông tin User")
            }

        } catch (e: Exception) {
            Resource.Error<FirebaseUser>(e.localizedMessage ?: "Đã xảy ra lỗi trong quá trình đăng ký")
        }
    }


    override suspend fun signIn(email: String, password: String): Resource<FirebaseUser> {
        return try {

            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user

            if (user != null) {
                Resource.Success(user)
            } else {
                Resource.Error<FirebaseUser>("Lỗi không xác định: Không lấy được thông tin User")
            }
        } catch (e: Exception) {
            Resource.Error<FirebaseUser>(e.localizedMessage ?: "Đã xảy ra lỗi trong quá trình đăng nhập")
        }
    }
    override fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    override fun signOut() {
        auth.signOut()
    }
}