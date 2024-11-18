package com.example.instademo


import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.instademo.model.PhotoDuel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.*
import java.util.concurrent.TimeUnit
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.*
import com.example.instademo.fragment.HomeFragment
import com.google.firebase.storage.FirebaseStorage


class PhotoDuelActivity : AppCompatActivity() {
    private lateinit var caption1: EditText
    private lateinit var caption2: EditText
    private lateinit var timeLimit: EditText
    private lateinit var btnPostDuel: Button
    private lateinit var image1: ImageView
    private lateinit var image2: ImageView
    private var image1Uri: Uri? = null
    private var image2Uri: Uri? = null

    private val PICK_IMAGE_1 = 1001
    private val PICK_IMAGE_2 = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_duel)

        caption1 = findViewById(R.id.caption1)
        caption2 = findViewById(R.id.caption2)
        timeLimit = findViewById(R.id.time_limit)
        btnPostDuel = findViewById(R.id.btn_post_duel)
        image1 = findViewById(R.id.image1)
        image2 = findViewById(R.id.image2)

        // Open gallery for Image 1 selection
        image1.setOnClickListener {
            openGallery(PICK_IMAGE_1)
        }

        // Open gallery for Image 2 selection
        image2.setOnClickListener {
            openGallery(PICK_IMAGE_2)
        }

        btnPostDuel.setOnClickListener {
            try {
                postDuel()
                startActivity(Intent(this, HomeFragment::class.java))
                finish()
            } catch (e: Exception) {
                Toast.makeText(this, "Error posting duel: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to open gallery
    private fun openGallery(requestCode: Int) {
        try {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, requestCode)
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening gallery: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        try {
            if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
                val selectedImageUri = data.data

                when (requestCode) {
                    PICK_IMAGE_1 -> {
                        image1Uri = selectedImageUri
                        image1.setImageURI(image1Uri)
                    }
                    PICK_IMAGE_2 -> {
                        image2Uri = selectedImageUri
                        image2.setImageURI(image2Uri)
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error selecting image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun postDuel() {
        try {
            val caption1Text = caption1.text.toString().trim()
            val caption2Text = caption2.text.toString().trim()
            val timeLimitText = timeLimit.text.toString().trim()

            if (caption1Text.isEmpty() || caption2Text.isEmpty() || timeLimitText.isEmpty() || image1Uri == null || image2Uri == null) {
                Toast.makeText(this, "Please fill in all fields and select images", Toast.LENGTH_SHORT).show()
                return
            }

            val timeLimitMinutes = timeLimitText.toLongOrNull()
            if (timeLimitMinutes == null || timeLimitMinutes <= 0) {
                Toast.makeText(this, "Invalid time limit", Toast.LENGTH_SHORT).show()
                return
            }

            val currentTime = System.currentTimeMillis()
            val duelEndTime = currentTime + TimeUnit.MINUTES.toMillis(timeLimitMinutes)

            // Upload images to Firebase Storage
            val storage = FirebaseStorage.getInstance()
            val image1Ref = storage.reference.child("photo_duels/${UUID.randomUUID()}_image1.jpg")
            val image2Ref = storage.reference.child("photo_duels/${UUID.randomUUID()}_image2.jpg")

            image1Uri?.let { uri1 ->
                image1Ref.putFile(uri1).addOnSuccessListener {
                    image1Ref.downloadUrl.addOnSuccessListener { url1 ->
                        image2Uri?.let { uri2 ->
                            image2Ref.putFile(uri2).addOnSuccessListener {
                                image2Ref.downloadUrl.addOnSuccessListener { url2 ->
                                    saveDuelToFirestore(caption1Text, caption2Text, currentTime, duelEndTime, url1.toString(), url2.toString())
                                }.addOnFailureListener {
                                    Toast.makeText(this, "Error getting image 2 URL: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                            }.addOnFailureListener {
                                Toast.makeText(this, "Error uploading image 2: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }.addOnFailureListener {
                        Toast.makeText(this, "Error getting image 1 URL: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Error uploading image 1: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error posting duel: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveDuelToFirestore(
        caption1: String,
        caption2: String,
        startTime: Long,
        endTime: Long,
        image1Url: String,
        image2Url: String
    ) {
        try {
            val photoDuel = PhotoDuel(
                image1Url = image1Url,
                image2Url = image2Url,
                caption1 = caption1,
                caption2 = caption2,
                startTime = startTime,
                endTime = endTime,
                userId = Firebase.auth.currentUser?.uid ?: "",
                votesImage1 = 0,
                votesImage2 = 0
            )

            FirebaseFirestore.getInstance().collection("photo_duels")
                .add(photoDuel)
                .addOnSuccessListener {
                    Toast.makeText(this, "Photo Duel Posted!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error saving duel: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving duel to Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
