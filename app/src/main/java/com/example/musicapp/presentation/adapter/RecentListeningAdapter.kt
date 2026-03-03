package com.example.musicapp.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.databinding.ItemRecentListeningBinding
import com.example.musicapp.domain.model.HomeItem
import com.example.musicapp.domain.model.Song

class RecentListeningAdapter(
    private val onItemClick: (Song) -> Unit
): ListAdapter<Song, RecentListeningAdapter.ViewHolder>(SongDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemRecentListeningBinding.inflate(
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

    inner class ViewHolder(private val binding: ItemRecentListeningBinding):
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Song) {
//            binding.ivCover = item.title

            Glide.with(binding.root.context)
                .load(item.coverUrl)
                .into(binding.ivCover)

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
    class SongDiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(
            oldItem: Song,
            newItem: Song
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Song,
            newItem: Song
        ): Boolean {
            return oldItem == newItem
        }
    }
}