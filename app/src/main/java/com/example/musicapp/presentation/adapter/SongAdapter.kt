package com.example.musicapp.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.musicapp.R
import com.example.musicapp.databinding.ItemSongBinding
import com.example.musicapp.domain.model.Song


class SongAdapter(
    private val onSongClick : (Song) -> Unit
): ListAdapter<Song, SongAdapter.SongViewHolder>(SongDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SongViewHolder {
        val binding = ItemSongBinding.inflate(
            LayoutInflater.from(parent.context),parent,false
        )
        return  SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
            holder.bind(getItem(position))
    }

    inner class SongViewHolder(private val binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(song: Song) {
            binding.tvTitle.text = song.title
            binding.tvArtist.text = song.artistName

            // Dùng thư viện Coil để load ảnh từ URL
            binding.ivCover.load(song.coverUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_launcher_background) // Ảnh tạm trong khi load
                error(R.drawable.ic_launcher_background) // Ảnh nếu lỗi
            }

            // Bắt sự kiện click vào item
            binding.root.setOnClickListener {
                onSongClick(song)
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



