package com.example.instademo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.instademo.adapter.ReelAdapter
import com.example.instademo.databinding.FragmentReelBinding
import com.example.instademo.model.Reel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class ReelFragment : Fragment() {

    private var _binding: FragmentReelBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ReelAdapter
    private val reelList = ArrayList<Reel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ReelAdapter(requireContext(), reelList)
        binding.viewPager1.orientation = ViewPager2.ORIENTATION_VERTICAL
        binding.viewPager1.adapter = adapter

        // Set up page change listener for preloading
        binding.viewPager1.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Preload when reaching the second-to-last item
                if (position >= reelList.size - 2) {
                    fetchReels()
                }
            }
        })

        // Show loading indicator initially
        binding.progressBar.isVisible = true

        fetchReels()
    }

    private fun fetchReels() {
        // Check if _binding is not null before proceeding
        if (_binding != null) {
            Firebase.firestore.collection("Reel").get()
                .addOnSuccessListener { documents ->
                    val tempList = ArrayList<Reel>()
                    for (document in documents) {
                        val reel = document.toObject<Reel>()
                        tempList.add(reel)
                    }
                    reelList.addAll(tempList)
                    reelList.reverse()
                    adapter.notifyItemRangeInserted(reelList.size - tempList.size, tempList.size)

                    // Hide loading indicator and show empty state if needed
                    binding.progressBar.isVisible = false
                    binding.emptyState.isVisible = reelList.isEmpty()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to load reels", Toast.LENGTH_SHORT).show()
                    binding.progressBar.isVisible = false
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
