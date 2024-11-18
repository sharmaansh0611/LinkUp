package com.example.instademo.post

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.instademo.HomeActivity
import com.example.instademo.databinding.ActivityAddPostBinding
import com.example.instademo.model.Post
import com.example.instademo.model.User
import com.example.instademo.utils.POST
import com.example.instademo.utils.POST_FOLDER
import com.example.instademo.utils.USER_NODE
import com.example.instademo.utils.uploadImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import java.util.*

class add_post : AppCompatActivity() {
    private lateinit var binding: ActivityAddPostBinding
    private var Url: String? = null
    private var scheduledTime: Long? = null  // Variable to store the scheduled time

    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            uploadImage(it, POST_FOLDER) { url ->
                if (url != null) {
                    binding.imageSelect.setImageURI(uri)
                    Url = url
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar4)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.toolbar4.setNavigationOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        binding.imageSelect.setOnClickListener {
            launcher.launch("image/*")
        }

        // Show date and time pickers when the user clicks on the date or time fields
        binding.scheduleDate.setOnClickListener {
            showDatePicker()
        }

        binding.scheduleTime.setOnClickListener {
            showTimePicker()
        }

        binding.post.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                FirebaseFirestore.getInstance().collection(USER_NODE).document(userId).get()
                    .addOnSuccessListener { document ->
                        val user = document.toObject<User>()
                        if (user != null && Url != null) {
                            val postId = FirebaseFirestore.getInstance().collection(POST).document().id

                            // If scheduledTime is not set, use the current time
                            val postTime = scheduledTime ?: System.currentTimeMillis()

                            val post = Post(
                                post = Url!!,
                                caption = binding.caption.text.toString(),
                                profileImageUrl = user.imageurl.toString(),
                                time = postTime.toString(),
                                uid = userId,
                                likesCount = 0,
                                likedBy = mutableListOf(),
                                id = postId,
                                scheduledTime = scheduledTime // Save the scheduled time (could be null)
                            )

                            FirebaseFirestore.getInstance().collection(POST).document(postId).set(post)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Post created successfully!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, HomeActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Failed to create post: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this, "Please select an image and enter a caption.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to get user data: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        binding.cancel.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    // Function to show date picker dialog
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                calendar.set(year, monthOfYear, dayOfMonth)
                binding.scheduleDate.setText("$dayOfMonth/${monthOfYear + 1}/$year")
                // After selecting date, prompt for time
                showTimePicker(calendar)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    // Function to show time picker dialog
    private fun showTimePicker(calendar: Calendar = Calendar.getInstance()) {
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                scheduledTime = calendar.timeInMillis
                binding.scheduleTime.setText(String.format("%02d:%02d", hourOfDay, minute))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }
}
