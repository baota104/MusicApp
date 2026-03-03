package com.example.musicapp.presentation.auth // Lưu ý: Của bạn đang là authen, nhớ check lại package name nhé

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
import com.example.musicapp.common.Resource
import com.example.musicapp.databinding.FragmentSignUpBinding
import com.example.musicapp.domain.usecase.LoginUseCase
import com.example.musicapp.domain.usecase.SignUpUseCase
import kotlinx.coroutines.launch

class SignUpFragment : Fragment() {
    private var _binding : FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private lateinit var authViewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val appcontainer = (requireActivity().application as MusicApplication)
        val authen = appcontainer.authenticationRepository
        val signUpUseCase = SignUpUseCase(authen)
        val loginUseCase = LoginUseCase(authen)
        val factory = AuthViewModel.Factory(signUpUseCase,loginUseCase)
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        setupClickListeners()
        setUpObserve()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Quá trình bấm nút Sign Up
        binding.btnSignUp.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val cfpass = binding.etConfirmPassword.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || cfpass.isEmpty()) {
                Toast.makeText(requireContext(), "Please, enter all the field", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != cfpass) {
                Toast.makeText(requireContext(), "Password doesn't match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            authViewModel.signUp(email, password, username)
        }

        binding.tvGoToSignIn.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setUpObserve() {
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.signUpState.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        Toast.makeText(requireContext(), "Sign Up Successfully!", Toast.LENGTH_SHORT).show()
                        authViewModel.resetSignUpState()
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                        authViewModel.resetSignUpState()
                    }
                    is Resource.Loading -> {
                    }
                    null -> {
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}