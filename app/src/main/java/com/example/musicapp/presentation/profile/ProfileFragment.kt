package com.example.musicapp.presentation.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.musicapp.MusicApplication
import com.example.musicapp.R
import com.example.musicapp.databinding.FragmentProfileBinding
import com.example.musicapp.domain.usecase.GetUserUseCase
import com.example.musicapp.domain.usecase.SignOutUseCase
import com.example.musicapp.presentation.auth.LoginFragment

class ProfileFragment : Fragment() {
   private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val appContainer = requireActivity().application as MusicApplication
        val authen = appContainer.authenticationRepository
        val getUserUseCase = GetUserUseCase(authen)
        val signOutUseCase = SignOutUseCase(authen)
        val factory = ProfileViewModel.Factory(signOutUseCase, getUserUseCase)
        viewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]

        setUpUI()
        setUpClick()
    }
    private fun setUpUI(){
        val user = viewModel.getUser()
        if (user != null) {
            binding.tvUserName.text = user.displayName ?: "Người yêu nhạc"
            binding.tvProfileEmail.text = user.email ?: "Chưa cập nhật email"
        } else {
            binding.tvUserName.text = "Khách"
            binding.tvProfileEmail.text = "Vui lòng đăng nhập lại"
        }
    }
    private fun setUpClick(){
        binding.cardMyProfile.setOnClickListener {
            Toast.makeText(requireContext(), "Clicked My Profile", Toast.LENGTH_SHORT).show()
        }
        binding.cardequalizer.setOnClickListener {
            Toast.makeText(requireContext(), "Clicked Equalizer", Toast.LENGTH_SHORT).show()
        }
        binding.cardSecurity.setOnClickListener {
            Toast.makeText(requireContext(), "Clicked Security", Toast.LENGTH_SHORT).show()
        }
        binding.cardLogout.setOnClickListener {
            viewModel.signOut()
            Toast.makeText(requireContext(), "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show()

            val navOptions = NavOptions.Builder()
                .setPopUpTo(findNavController().graph.startDestinationId, true)
                .build()
            findNavController().navigate(
                R.id.loginFragment, // Đảm bảo ID này giống trong nav_graph.xml
                null,
                navOptions
            )
        }
        binding.btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}