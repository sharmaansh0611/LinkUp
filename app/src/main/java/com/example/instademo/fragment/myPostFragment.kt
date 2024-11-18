package com.example.instademo.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.instademo.adapter.MyPostRvAdapter
import com.example.instademo.databinding.FragmentMyPostBinding
import com.example.instademo.model.Post
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class myPostFragment : Fragment() {
    private lateinit var binding: FragmentMyPostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyPostBinding.inflate(inflater, container, false)

        val postList = ArrayList<Post>()
        val adapter = MyPostRvAdapter(requireContext(), postList)

        binding.rvMypost.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        binding.rvMypost.adapter = adapter

        val firestore = Firebase.firestore
        val auth = Firebase.auth

        auth.currentUser?.let { user ->
            firestore.collection(user.uid).get()
                .addOnSuccessListener { result ->
                    val tempList = arrayListOf<Post>()
                    for (document in result) {
                        tempList.add(Post(document.getString("post") ?: "", document.getString("caption") ?: ""))
                    }
                    adapter.postList = tempList
                    adapter.notifyDataSetChanged()
                }
        }

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = myPostFragment()
    }
}
