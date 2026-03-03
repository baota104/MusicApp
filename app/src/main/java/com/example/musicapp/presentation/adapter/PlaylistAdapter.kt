package com.example.musicapp.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.databinding.ItemLibraryBinding
import com.example.musicapp.domain.model.Genre
import com.example.musicapp.domain.model.Playlist

class PlaylistAdapter(
    private val onclicked :(Playlist) -> Unit
): ListAdapter<Playlist, PlaylistAdapter.ViewHolder>(PlaylistDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemLibraryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemLibraryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Playlist) {
            binding.tvTitle.text = item.name

            Glide.with(binding.root.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(binding.ivCover)

            binding.root.setOnClickListener {
               onclicked(item)
            }
        }
    }

    class PlaylistDiffCallback : DiffUtil.ItemCallback<Playlist>(){
        override fun areItemsTheSame(
            oldItem: Playlist,
            newItem: Playlist
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Playlist,
            newItem: Playlist
        ): Boolean {
            return oldItem == newItem
        }

    }

}