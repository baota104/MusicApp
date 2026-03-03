package com.example.musicapp.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.musicapp.common.Resource
import com.example.musicapp.domain.usecase.LoginUseCase
import com.example.musicapp.domain.usecase.SignUpUseCase
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val signUpUseCase: SignUpUseCase,
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    // Quản lý trạng thái đăng ký
    private val _signUpState = MutableStateFlow<Resource<FirebaseUser>?>(null)
    val signUpState: StateFlow<Resource<FirebaseUser>?> = _signUpState

    private val _loginState = MutableStateFlow<Resource<FirebaseUser>?>(null)
    val loginState: StateFlow<Resource<FirebaseUser>?> = _loginState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            val result = loginUseCase(email, password)
            _loginState.value = result
        }
    }

    fun resetLoginState() {
        _loginState.value = null
    }
    fun signUp(email: String, password: String, username: String) {
        viewModelScope.launch {
            _signUpState.value = Resource.Loading() // Báo cho UI hiện vòng xoay (Loading)
            val result = signUpUseCase(email, password, username)
            _signUpState.value = result
        }
    }


    fun resetSignUpState() {
        _signUpState.value = null
    }

    class Factory(
        private val signUpUseCase: SignUpUseCase,
        private val loginUseCase: LoginUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(signUpUseCase,loginUseCase) as T
        }
    }
}