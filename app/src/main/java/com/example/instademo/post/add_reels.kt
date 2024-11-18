package com.example.instademo.post

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.instademo.HomeActivity
import com.example.instademo.databinding.ActivityAddReelsBinding
import com.example.instademo.model.Reel
import com.example.instademo.model.User
import com.example.instademo.utils.REEL
import com.example.instademo.utils.REEL_FOLDER
import com.example.instademo.utils.USER_NODE
import com.example.instademo.utils.uploadVideo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

class add_reels : AppCompatActivity() {
    private lateinit var binding: ActivityAddReelsBinding
    private lateinit var pd: ProgressDialog
    private var videourl: String? = null

    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            uploadVideo(uri, REEL_FOLDER, this@add_reels, pd) { url ->
                if (url != null) {
                    videourl = url
                    Toast.makeText(this, "Video uploaded successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to upload video", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReelsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pd = ProgressDialog(this)

        setSupportActionBar(binding.toolbar3)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.toolbar3.setNavigationOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
        binding.cancel1.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
        binding.addReels.setOnClickListener {
            launcher.launch("video/*")
        }

        binding.post1.setOnClickListener {
            val videoUrl = videourl
            val caption = binding.caption1.text.toString()

            if (videoUrl.isNullOrEmpty()) {showMessage("Please select a video")
                return@setOnClickListener
            }
            if (caption.isEmpty()) {
                showMessage("Please add a caption")
                return@setOnClickListener
            }

            Firebase.firestore.collection(USER_NODE).document(FirebaseAuth.getInstance().currentUser!!.uid).get()
                .addOnSuccessListener { documentSnapshot ->

                    val user = documentSnapshot.toObject<com.example.instademo.model.User>()!!
                    val reel = Reel(videoUrl, caption, user.imageurl)
                    saveReelToFirestore(reel)
                }
                .addOnFailureListener { e ->showMessage("Failed to get user data: ${e.message}")
                }
        }
    }

    private fun saveReelToFirestore(reel: Reel) {
        FirebaseFirestore.getInstance().collection(REEL).document().set(reel)
            .addOnSuccessListener {
                FirebaseFirestore.getInstance().collection(FirebaseAuth.getInstance().currentUser!!.uid + REEL)
                    .document().set(reel)
                    .addOnSuccessListener {
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        showMessage("Failed to post reel: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                showMessage("Failed to post reel: ${e.message}")
            }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
