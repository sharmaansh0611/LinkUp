package com.example.instademo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.instademo.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore

class signIn : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Check if user is already logged in
        if (auth.currentUser != null) {
            // User is already logged in, navigate to HomeActivity
            navigateToHome()
        }

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.googlesignIn.setOnClickListener {
            signInWithGoogle()
        }

        binding.noAccount.setOnClickListener {
            val intent = Intent(this, signUp::class.java)
            startActivity(intent)
        }

        binding.login.setOnClickListener {
            val email: String = binding.email.text.toString()
            val password: String = binding.password.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_LONG).show()
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "User logged in", Toast.LENGTH_LONG).show()
                            navigateToHome()
                        } else {
                            Toast.makeText(this, "User not logged in", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
                fetchGoogleUserData(account)  // Fetch and handle user data
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "User logged in with Google", Toast.LENGTH_LONG).show()
                    navigateToHome()
                } else {
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun fetchGoogleUserData(account: GoogleSignInAccount) {
        val userName = account.displayName
        val userEmail = account.email
        val userPhotoUrl = account.photoUrl?.toString()

        // Save this data to your database or pass it to another activity
        saveUserDataToFirestore(userName, userEmail, userPhotoUrl)
    }

    private fun saveUserDataToFirestore(name: String?, email: String?, photoUrl: String?) {
        val db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid

        val userMap = hashMapOf(
            "name" to name,
            "email" to email,
            "photoUrl" to photoUrl
        )

        if (userId != null) {
            db.collection("users").document(userId)
                .set(userMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "User data saved", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save user data", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
