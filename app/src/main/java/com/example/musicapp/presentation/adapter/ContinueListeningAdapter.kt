package com.example.musicapp.presentation.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.databinding.ItemContinueListeningBinding
import com.example.musicapp.domain.model.HomeItem

class ContinueListeningAdapter(
    private val onItemClick: (HomeItem) -> Unit
) : ListAdapter<HomeItem, ContinueListeningAdapter.ViewHolder>(HomeItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContinueListeningBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemContinueListeningBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HomeItem) {
            binding.tvTitle.text = item.title

            Glide.with(binding.root.context)
                .load(item.imageUrl)
                .into(binding.ivCover)

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    class HomeItemDiffCallback : DiffUtil.ItemCallback<HomeItem>() {
        override fun areItemsTheSame(oldItem: HomeItem, newItem: HomeItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: HomeItem, newItem: HomeItem): Boolean {
            return oldItem == newItem
        }
    }
}