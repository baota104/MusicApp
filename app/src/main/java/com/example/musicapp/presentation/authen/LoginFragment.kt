package com.example.musicapp.presentation.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.musicapp.MusicApplication
import com.example.musicapp.R
import com.example.musicapp.common.Resource
import com.example.musicapp.databinding.FragmentLoginBinding // Đảm bảo bạn đã có file XML này
import com.example.musicapp.domain.usecase.LoginUseCase
import com.example.musicapp.domain.usecase.SignUpUseCase
import com.example.musicapp.presentation.home.HomeFragment
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var authViewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appContainer = (requireActivity().application as MusicApplication)
        val authRepo = appContainer.authenticationRepository
        val signUpUseCase = SignUpUseCase(authRepo)
        val loginUseCase = LoginUseCase(authRepo)
        val factory = AuthViewModel.Factory(signUpUseCase, loginUseCase)
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        setupClickListeners()
        setupObserve()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please, enter all the field", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.login(email, password)
        }

        binding.tvGoToSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }
    }

    private fun setupObserve() {
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.loginState.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        Toast.makeText(requireContext(), "Login successfully!", Toast.LENGTH_SHORT).show()
                        authViewModel.resetLoginState()

                        val navOptions = androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.loginFragment, true)
                            .build()
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment,
                            null,
                            navOptions
                        )
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                        authViewModel.resetLoginState()
                    }
                    is Resource.Loading -> {
                    }
                    null -> {}
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}