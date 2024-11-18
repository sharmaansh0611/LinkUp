package com.example.instademo.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instademo.adapter.SearchAdapter
import com.example.instademo.databinding.FragmentSearchBinding
import com.example.instademo.model.User
import com.example.instademo.utils.USER_NODE
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var adapter: SearchAdapter
    private var searchList: ArrayList<User> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        // Setup RecyclerView
        binding.rvsearch.layoutManager = LinearLayoutManager(requireContext())
        adapter = SearchAdapter(requireContext(), searchList)
        binding.rvsearch.adapter = adapter

        // Load all users initially
        loadAllUsers()

        // Search button click listener
        binding.searchButton.setOnClickListener {
            val text = binding.search.text.toString().trim()
            if (text.isNotEmpty()) {
                searchUsersByName(text)
            } else {
                Toast.makeText(requireContext(), "Please enter a name to search", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun loadAllUsers() {
        Firebase.firestore.collection(USER_NODE).get().addOnSuccessListener {
            val temp = ArrayList<User>()
            for (document in it.documents) {
                if (document.id == Firebase.auth.currentUser?.uid) continue

                val user = document.toObject<User>()
                if (user != null) {
                    temp.add(user)
                }
            }
            searchList.clear()
            searchList.addAll(temp)
            adapter.notifyDataSetChanged()

            // Show or hide the "No User Found" message
            toggleNoUserMessage()
        }
    }

    private fun searchUsersByName(name: String) {
        Firebase.firestore.collection(USER_NODE).whereEqualTo("name", name).get().addOnSuccessListener {
            val temp = ArrayList<User>()
            searchList.clear()
            if (!it.isEmpty) {
                for (document in it.documents) {
                    if (document.id == Firebase.auth.currentUser?.uid) continue

                    val user = document.toObject<User>()
                    if (user != null) {
                        temp.add(user)
                    }
                }
                searchList.addAll(temp)
            } else {
                Toast.makeText(requireContext(), "No users found", Toast.LENGTH_SHORT).show()
            }
            adapter.notifyDataSetChanged()

            // Show or hide the "No User Found" message
            toggleNoUserMessage()
        }.addOnFailureListener {
            Log.e("SearchFragment", "Error fetching users", it)
            Toast.makeText(requireContext(), "Error searching users", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleNoUserMessage() {
        if (searchList.isEmpty()) {
            binding.noUserFoundText.visibility = View.VISIBLE
            binding.rvsearch.visibility = View.GONE
        } else {
            binding.noUserFoundText.visibility = View.GONE
            binding.rvsearch.visibility = View.VISIBLE
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = SearchFragment()
    }
}
