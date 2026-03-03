package com.example.musicapp.presentation.search.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.databinding.ItemSearchGenreBinding
import com.example.musicapp.domain.model.Genre

class GenreAdapter(
    private val onItemClick: (Genre) -> Unit
) : ListAdapter<Genre, GenreAdapter.ViewHolder>(GenreDiffCallback()) {

    private val cardColors = listOf(
        "#E13300", "#7358FF", "#1E3264", "#E8115B",
        "#148A08", "#E1118C", "#8D67AB", "#509BF5",
        "#FF4632", "#B49BC8", "#006450", "#E91429"
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchGenreBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class ViewHolder(private val binding: ItemSearchGenreBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(genre: Genre, position: Int) {
            binding.tvGenreName.text = genre.name

            Glide.with(binding.root.context)
                .load(genre.pictureUrl)
                .into(binding.ivGenreCover)

            val colorHex = cardColors[position % cardColors.size]
            binding.root.setCardBackgroundColor(Color.parseColor(colorHex))

            binding.root.setOnClickListener {
                onItemClick(genre)
            }
        }
    }

    class GenreDiffCallback : DiffUtil.ItemCallback<Genre>() {
        override fun areItemsTheSame(oldItem: Genre, newItem: Genre) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Genre, newItem: Genre) = oldItem == newItem
    }
}