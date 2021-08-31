package com.ldept.simplepass.database

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ldept.simplepass.R


class PasswordListAdapter(
    private val listener : OnItemClickListener
    ) : ListAdapter<PasswordEntry, PasswordListAdapter.PasswordViewHolder>(DiffCallback()) {

    private var data = listOf<PasswordEntry>()

    class DiffCallback : DiffUtil.ItemCallback<PasswordEntry>() {
        override fun areContentsTheSame(oldItem: PasswordEntry, newItem: PasswordEntry): Boolean =
            oldItem.id == newItem.id

        override fun areItemsTheSame(oldItem: PasswordEntry, newItem: PasswordEntry): Boolean =
            oldItem == newItem
    }

    inner class PasswordViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        val nameText : TextView = itemView.findViewById(R.id.passwordRecyclerTextView)
        init{
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            listener.onItemClick(data[adapterPosition])
        }
    }

    override fun onBindViewHolder(holder: PasswordViewHolder, position: Int) {
        val item : PasswordEntry = data[position]
//        val res = holder.itemView.context.resources
        holder.nameText.text = item.name
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setPasswords(list : List<PasswordEntry>) {
        this.data = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasswordViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(
            R.layout.password_recyclerview_item,
            parent, false)
        return PasswordViewHolder(view)
    }

    interface OnItemClickListener {
        fun onItemClick(password : PasswordEntry)
    }

}
