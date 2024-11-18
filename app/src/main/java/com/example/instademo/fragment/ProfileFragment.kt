package com.example.instademo.fragment

import android.content.Intent
import com.google.android.material.snackbar.Snackbar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.instademo.R
import com.example.instademo.adapter.ViewPagerAdapter
import com.example.instademo.databinding.FragmentProfileBinding
import com.example.instademo.model.User
import com.example.instademo.signUp
import com.example.instademo.utils.USER_NODE
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import android.view.*
import com.example.instademo.signIn
import com.google.firebase.auth.FirebaseAuth
import android.widget.PopupMenu


class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var isPrivateProfile = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Edit Profile Button
        binding.editProfile.setOnClickListener {
            val intent = Intent(activity, signUp::class.java)
            intent.putExtra("MODE", "EDIT")
            startActivity(intent)
        }

        // Setup ViewPager and TabLayout
        setupViewPager()

        // Handle overflow menu click (3 dots icon)
        binding.overflowMenu.setOnClickListener { showPopupMenu(it) }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        loadProfile()
    }

    private fun setupViewPager() {
        val viewPagerAdapter = ViewPagerAdapter(requireActivity().supportFragmentManager)
        viewPagerAdapter.addFragment(myPostFragment(), "My Post")
        viewPagerAdapter.addFragment(myReelsFragment(), "My Reels")
        binding.viewPager.adapter = viewPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }

    private fun loadProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection(USER_NODE).document(currentUser.uid).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.toObject(User::class.java)
                        user?.let {
                            binding.name.text = it.name
                            binding.bio.text = it.bio
                            if (!it.imageurl.isNullOrEmpty()) {
                                Picasso.get().load(it.imageurl).into(binding.profileImage)
                            }
                            isPrivateProfile = it.isPrivate ?: false // Assuming "isPrivate" is a Boolean in User model
                        }
                    }
                }
        }
    }

    private fun showPopupMenu(view: View) {
        // Create a PopupMenu
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.profile_menu, popupMenu.menu)

        // Update the privacy toggle based on current state
        popupMenu.menu.findItem(R.id.action_toggle_privacy).title =
            if (isPrivateProfile) "Make Profile Public" else "Make Profile Private"

        // Handle menu item clicks
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
                    auth.signOut()
                    val intent = Intent(activity, signIn::class.java)
                    startActivity(intent)
                    activity?.finish()  // Close the current activity
                    true
                }
                R.id.action_toggle_privacy -> {
                    toggleProfilePrivacy()
                    true
                }
                else -> false
            }
        }

        // Show the popup menu
        popupMenu.show()
    }

    private fun toggleProfilePrivacy() {
        val currentUser = auth.currentUser
        currentUser?.let {
            val userDocRef = db.collection(USER_NODE).document(it.uid)
            isPrivateProfile = !isPrivateProfile

            // Update the user's privacy setting in Firestore
            userDocRef.update("isPrivate", isPrivateProfile)
                .addOnSuccessListener {
                    val message = if (isPrivateProfile) "Profile is now Private" else "Profile is now Public"
                    binding.root.showSnackbar(message)
                }
                .addOnFailureListener {
                    binding.root.showSnackbar("Failed to update profile privacy")
                }
        }
    }

    fun View.showSnackbar(message: String) {
        Snackbar.make(this, message, Snackbar.LENGTH_SHORT).show()
    }
}
