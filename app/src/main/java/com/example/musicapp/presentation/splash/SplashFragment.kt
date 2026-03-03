package com.example.musicapp.presentation.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.musicapp.MusicApplication
import com.example.musicapp.R
import com.example.musicapp.domain.usecase.CheckLoginStatusUseCase
import com.example.musicapp.domain.usecase.LoginUseCase
import com.example.musicapp.domain.usecase.SignUpUseCase
import com.example.musicapp.presentation.auth.AuthViewModel
import com.example.musicapp.presentation.auth.LoginFragment // Màn hình Login
import com.example.musicapp.presentation.library.LibraryFragment // Màn hình Chính (Home)
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private lateinit var splashViewModel: SplashViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Nhớ đảm bảo bạn có file fragment_splash.xml như đã làm ở bước trước
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Khởi tạo ViewModel
        val appContainer = (requireActivity().application as MusicApplication)
        val authRepo = appContainer.authenticationRepository
        val checkLoginStatusUseCase = CheckLoginStatusUseCase(authRepo)
        val factory = SplashViewModel.Factory(checkLoginStatusUseCase)
        splashViewModel = ViewModelProvider(this, factory)[SplashViewModel::class.java]

        viewLifecycleOwner.lifecycleScope.launch {
            delay(2000)
            if (splashViewModel.isUserLoggedIn()) {
                val navOptions = androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.splashFragment, true)
                    .build()
                findNavController().navigate(R.id.action_splashFragment_to_homeFragment,
                    null,
                    navOptions
                )
            } else {
                val navOptions = androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.splashFragment, true)
                    .build()
                findNavController().navigate(R.id.action_splashFragment_to_loginFragment,
                    null,
                    navOptions
                )
            }
        }
    }
}