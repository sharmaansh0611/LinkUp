package com.example.instademo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.instademo.databinding.MyPostDesignRvBinding
import com.example.instademo.model.Post
import com.squareup.picasso.Picasso

class MyPostRvAdapter(
    var context: Context,
    var postList: ArrayList<Post>
    
) : RecyclerView.Adapter<MyPostRvAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: MyPostDesignRvBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: String) {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MyPostDesignRvBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.postImageView
        Picasso.get().load(postList[position].post).into(holder.binding.postImageView)
    }

    override fun getItemCount(): Int {
        return postList.size
    }
}


