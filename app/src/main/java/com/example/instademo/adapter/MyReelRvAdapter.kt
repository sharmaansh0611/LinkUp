package com.example.instademo.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.instademo.databinding.MyPostDesignRvBinding
import com.example.instademo.model.Post
import com.example.instademo.model.Reel
import com.squareup.picasso.Picasso

class MyReelRvAdapter(
    var context: Context,
    var reelList: ArrayList<Reel>

) : RecyclerView.Adapter<MyReelRvAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: MyPostDesignRvBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MyPostDesignRvBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.postImageView
        Glide.with(context).load(reelList.get(position).videoUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.binding.postImageView);
    }

    override fun getItemCount(): Int {
        return reelList.size
    }
}


