package com.example.instademo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.instademo.databinding.ActivitySignUpBinding
import com.example.instademo.model.User
import com.example.instademo.utils.USER_NODE
import com.example.instademo.utils.USER_PROFILE_FOLDER
import com.example.instademo.utils.uploadImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlin.random.Random

class signUp : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    lateinit var user: User

    private val randomBios = arrayOf(
        "Living the dream!", "Feeling fantastic!", "Adventurer at heart",
        "Coffee first, conquer the world later", "Tech geek by day, artist by night",
        "Explorer of the unknown", "Chasing dreams one step at a time",
        "Creator of my own destiny", "Lover of life and good vibes",
        "Learning every day", "Always on the go", "Happiness is homemade",
        "In pursuit of perfection", "Dream big, hustle harder"
    )

    // Image picker launcher
    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            uploadImage(uri, USER_PROFILE_FOLDER) { downloadUrl ->
                if (downloadUrl != null) {
                    user.imageurl = downloadUrl
                    binding.profileImage.setImageURI(uri)
                }
            }
        }
    }

    // Google Sign-In launcher
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleGoogleSignInResult(task)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        user = User()

        // Assign random bio
        user.bio = generateRandomBio()

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Handle account update
        if (intent.hasExtra("MODE") && intent.getStringExtra("MODE").equals("EDIT")) {
            binding.createAccount.text = "Update"

            // Hide Google Sign-In and "Already a User?" options in EDIT mode
            binding.googleSignInButton.visibility = View.GONE
            binding.alreadyUser.visibility = View.GONE

            FirebaseFirestore.getInstance().collection(USER_NODE)
                .document(auth.currentUser!!.uid).get()
                .addOnCompleteListener {
                    val fetchedUser = it.result.toObject(User::class.java)!!
                    if (!fetchedUser.imageurl.isNullOrEmpty()) {
                        Picasso.get().load(fetchedUser.imageurl).into(binding.profileImage)
                    }
                    binding.name.setText(fetchedUser.name)
                    binding.email.setText(fetchedUser.email)
                    user = fetchedUser
                }
        }

        binding.alreadyUser.setOnClickListener {
            val intent = Intent(this, signIn::class.java)
            startActivity(intent)
        }

        binding.addImage.setOnClickListener {
            launcher.launch("image/*")
        }

        binding.createAccount.setOnClickListener {
            val name = binding.name.text.toString()
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (!email.matches(emailRegex.toRegex())) {
                Toast.makeText(this, "Check your email", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (intent.hasExtra("MODE") && intent.getStringExtra("MODE").equals("EDIT")) {
                // Update the user object with new values
                user.name = name
                user.email = email

                FirebaseFirestore.getInstance().collection(USER_NODE)
                    .document(user.id.toString()).set(user)
                    .addOnSuccessListener {
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
            } else {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            user.name = name
                            user.email = email
                            user.id = auth.currentUser?.uid

                            FirebaseFirestore.getInstance().collection(USER_NODE)
                                .document(user.id.toString()).set(user)
                                .addOnSuccessListener {
                                    val intent = Intent(this, HomeActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                        } else {
                            Toast.makeText(this, "User not created", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }

        binding.googleSignInButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                firebaseAuthWithGoogle(account.idToken!!)
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to generate random bio
    private fun generateRandomBio(): String {
        val index = Random.nextInt(randomBios.size)
        return randomBios[index]
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val user = User().apply {
                        id = firebaseUser?.uid
                        name = firebaseUser?.displayName ?: ""
                        email = firebaseUser?.email ?: ""
                        imageurl = firebaseUser?.photoUrl.toString()
                        bio = generateRandomBio()  // Assign random bio for Google sign-in
                    }

                    FirebaseFirestore.getInstance().collection(USER_NODE)
                        .document(user.id.toString()).set(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to create user in Firestore", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
