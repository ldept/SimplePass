package com.ldept.simplepass.ui.PasswordListFragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ldept.simplepass.data.Entities.PasswordEntry
import com.ldept.simplepass.databinding.PasswordItemBinding

class PasswordListAdapter (
    private val listener : OnItemClickListener
        ) : ListAdapter<PasswordEntry, PasswordListAdapter.PasswordViewHolder>(DiffCallback()) {

    inner class PasswordViewHolder(
        private val binding: PasswordItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    // It's possible to click a deleted item before an animation finishes
                    // that's why we have to check if the item we clicked is valid or not
                    if(adapterPosition != RecyclerView.NO_POSITION){
                        val passwordEntry = getItem(adapterPosition)
                        listener.onItemClick(passwordEntry)
                    }
                }
            }
        }

        fun bind(passwordEntry: PasswordEntry) {
            binding.apply {
                passwordName.text = passwordEntry.name
            }
        }


    }

    interface OnItemClickListener {
        fun onItemClick(password : PasswordEntry)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasswordViewHolder {
        val binding = PasswordItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PasswordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PasswordViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class DiffCallback : DiffUtil.ItemCallback<PasswordEntry>() {
        override fun areContentsTheSame(oldItem: PasswordEntry, newItem: PasswordEntry): Boolean =
            oldItem == newItem

        override fun areItemsTheSame(oldItem: PasswordEntry, newItem: PasswordEntry): Boolean =
            oldItem.id == newItem.id
    }


}