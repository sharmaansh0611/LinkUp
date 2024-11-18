package com.example.instademo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instademo.R
import com.example.instademo.databinding.FollowRvBinding
import com.example.instademo.model.User

class FollowRvAdapter(var context: Context, var followList: ArrayList<User>): RecyclerView.Adapter<FollowRvAdapter.ViewHolder>() {

    inner class ViewHolder(var binding: FollowRvBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FollowRvBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context)
            .load(followList[position].imageurl)
            .placeholder(R.drawable.ic_person)
            .into(holder.binding.storyImg) // Changed to camelCase to match the binding variable
        holder.binding.storyUser.text = followList[position].name // Changed to camelCase to match the binding variable
    }

    override fun getItemCount(): Int {
        return followList.size
    }
}
