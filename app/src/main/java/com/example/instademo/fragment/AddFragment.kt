package com.example.instademo.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.instademo.R
import com.example.instademo.databinding.FragmentAddBinding
import com.example.instademo.post.add_post
import com.example.instademo.post.add_reels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentAddBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddBinding.inflate(inflater, container, false)

        // Set click listeners
        binding.addImage.setOnClickListener {
            try {
                Log.d("AddFragment", "Add Image button clicked")
                val intent = Intent(requireContext(), add_post::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("AddFragment", "Error starting add_post activity", e)
                Toast.makeText(requireContext(), "Failed to open add image screen", Toast.LENGTH_SHORT).show()
            }
        }

        binding.addReels.setOnClickListener {
            try {
                Log.d("AddFragment", "Add Reels button clicked")
                val intent = Intent(requireContext(), add_reels::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("AddFragment", "Error starting add_reels activity", e)
                Toast.makeText(requireContext(), "Failed to open add reels screen", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    companion object {}
}
