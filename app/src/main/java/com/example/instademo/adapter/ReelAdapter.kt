package com.example.instademo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.instademo.R
import com.example.instademo.databinding.ReelDgBinding
import com.example.instademo.model.Reel
import com.squareup.picasso.Picasso

class ReelAdapter(var context: Context, var reelList: ArrayList<Reel>) :
    RecyclerView.Adapter<ReelAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ReelDgBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ReelDgBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var reel = reelList.get(position)

        // Load profile image with error handling

        Picasso.get().load(reel.profile)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_error) // Placeholder for errors
                .into(holder.binding.profileImage)


        holder.binding.tv1.text = reel.caption

        // Show progress bar while video is loading
        holder.binding.progressBar.visibility = View.VISIBLE

        holder.binding.videoView.setVideoPath(reel.videoUrl)
        holder.binding.videoView.setOnPreparedListener {
            holder.binding.progressBar.visibility = View.GONE
            holder.binding.videoView.start()
        }

        // Reset video state when view is recycled
        holder.binding.videoView.setOnCompletionListener {
            holder.binding.progressBar.visibility = View.VISIBLE
            holder.binding.videoView.stopPlayback()
        }
    }

    override fun getItemCount(): Int {
        return reelList.size
    }
}