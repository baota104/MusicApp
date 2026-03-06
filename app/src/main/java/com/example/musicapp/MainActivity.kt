package com.example.musicapp

import android.content.ComponentName
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.musicapp.databinding.ActivityMainBinding
import com.example.musicapp.presentation.player.NowPlayingFragment
import com.example.musicapp.service.MusicService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpNavigation()
        initializeController()
        setupClickListeners()

    }
    private fun setUpNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController


        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.splashFragment,
                R.id.loginFragment,
                R.id.signUpFragment -> {
                    binding.bottomNavigation.visibility = View.GONE
                    binding.miniPlayerCard.visibility = View.GONE
                }
                else -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                }
            }
        }
    }
//
    private fun initializeController() {
        val sessionToken = SessionToken(this, ComponentName(this, MusicService::class.java))
        mediaControllerFuture = MediaController.Builder(this, sessionToken).buildAsync()

        mediaControllerFuture?.addListener({
            mediaController = mediaControllerFuture?.get()
            mediaController?.addListener(playerListener)
            updateMiniPlayerUI()
        }, MoreExecutors.directExecutor())
    }

    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updateMiniPlayerUI()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                binding.btnMiniPlayPause.setImageResource(R.drawable.ic_pause)
            } else {
                binding.btnMiniPlayPause.setImageResource(R.drawable.ic_play)
            }
        }
    }

    private fun updateMiniPlayerUI() {
        val player = mediaController ?: return
        if (player.currentMediaItem == null) {
            binding.miniPlayerCard.visibility = View.GONE
            return
        }

        binding.miniPlayerCard.visibility = View.VISIBLE

        val metadata = player.currentMediaItem?.mediaMetadata
        binding.tvMiniTitle.text = metadata?.title ?: "Unknown Song"
        binding.tvMiniArtist.text = metadata?.artist ?: "Unknown Artist"

        metadata?.artworkUri?.let { uri ->
            Glide.with(this)
                .load(uri)
                .into(binding.ivMiniCover)
        }

        if (player.isPlaying) {
            binding.btnMiniPlayPause.setImageResource(R.drawable.ic_pause)
        } else {
            binding.btnMiniPlayPause.setImageResource(R.drawable.ic_play)
        }
    }

    private fun setupClickListeners() {
        binding.btnMiniPlayPause.setOnClickListener {
            mediaController?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                } else {
                    player.play()
                }
            }
        }


        binding.miniPlayerCard.setOnClickListener {
            val nowPlayingFragment = NowPlayingFragment()
            nowPlayingFragment.show(supportFragmentManager, "NowPlayingFragment")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaController?.removeListener(playerListener)
        mediaControllerFuture?.let { MediaController.releaseFuture(it) }
    }
}