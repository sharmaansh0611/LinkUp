package com.example.instademo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instademo.R
import com.example.instademo.databinding.ItemSearchBinding
import com.example.instademo.model.User
import com.example.instademo.utils.FOLLOW
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SearchAdapter(
    private val context: Context,
    private var userList: ArrayList<User>
) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemSearchBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        var isFollow = false

        Glide.with(context).load(user.imageurl).placeholder(R.drawable.ic_person).into(holder.binding.userImage)
        holder.binding.profilename.text = user.name

        // Check if the user is followed
        Firebase.firestore.collection(Firebase.auth.currentUser!!.uid + FOLLOW)
            .whereEqualTo("email", user.email).get().addOnSuccessListener {
                if (it.documents.isNotEmpty()) {
                    holder.binding.follow.text = "Unfollow"
                    isFollow = true
                } else {
                    holder.binding.follow.text = "Follow"
                    isFollow = false
                }
            }

        holder.binding.follow.setOnClickListener {
            if (isFollow) {
                // Unfollow user
                Firebase.firestore.collection(Firebase.auth.currentUser!!.uid + FOLLOW)
                    .whereEqualTo("email", user.email).get().addOnSuccessListener {
                        if (it.documents.isNotEmpty()) {
                            Firebase.firestore.collection(Firebase.auth.currentUser!!.uid + FOLLOW)
                                .document(it.documents[0].id).delete().addOnSuccessListener {
                                    holder.binding.follow.text = "Follow"
                                    isFollow = false
                                }
                        }
                    }
            } else {
                // Follow user
                Firebase.firestore.collection(Firebase.auth.currentUser!!.uid + FOLLOW)
                    .add(user).addOnSuccessListener {
                        holder.binding.follow.text = "Unfollow"
                        isFollow = true
                    }
            }
        }
    }
}
