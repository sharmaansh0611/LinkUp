package com.example.instademo.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instademo.Aboutus
import com.example.instademo.PhotoDuelActivity
import com.example.instademo.R
import com.example.instademo.adapter.FollowRvAdapter
import com.example.instademo.adapter.PostAdapter
import com.example.instademo.adapter.PostDuelAdapter
import com.example.instademo.databinding.FragmentHomeBinding
import com.example.instademo.model.Post
import com.example.instademo.model.User
import com.example.instademo.utils.FOLLOW
import com.example.instademo.utils.POST
import com.example.instademo.utils.USER_NODE
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso


import com.example.instademo.model.PhotoDuel


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val postList = ArrayList<Post>()
    private val duelList = ArrayList<PhotoDuel>() // List for duels
    private lateinit var postAdapter: PostAdapter
    private lateinit var duelAdapter: PostDuelAdapter // Adapter for duels
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.option_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.aboutUs -> {
                val intent = Intent(requireContext(), Aboutus::class.java)
                startActivity(intent)
                true
            }
            R.id.chat -> {
                // Handle chat item click
                true
            }
            R.id.photo_duel -> {
                // Handle chat item click
                val intent = Intent(requireContext(), PhotoDuelActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        firestore = FirebaseFirestore.getInstance()

        // Initialize RecyclerView for posts
        postAdapter = PostAdapter(requireContext(), postList)
        binding.rv9.layoutManager = LinearLayoutManager(requireContext())
        binding.rv9.adapter = postAdapter

        // Initialize RecyclerView for duels
        duelAdapter = PostDuelAdapter(requireContext(), duelList)
        binding.duelRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.duelRv.adapter = duelAdapter

        // Initialize RecyclerView for follows
        val followList = ArrayList<User>()
        val followRvAdapter = FollowRvAdapter(requireContext(), followList)
        binding.followRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.followRv.adapter = followRvAdapter

        // Set Toolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.materialToolbar12)

        // Load current user's profile image
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            firestore.collection(USER_NODE).document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    val user = document.toObject(User::class.java)
                    if (user != null && !user.imageurl.isNullOrEmpty()) {
                        Picasso.get().load(user.imageurl).into(binding.userImage)
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Error fetching user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Fetch follows, duels, and posts
        fetchFollows(currentUser, followList, followRvAdapter)
        fetchDuels()
        fetchPosts()

        return binding.root
    }

    private fun fetchFollows(currentUser: com.google.firebase.auth.FirebaseUser?, followList: ArrayList<User>, adapter: FollowRvAdapter) {
        if (currentUser != null) {
            firestore.collection("${currentUser.uid}$FOLLOW").get()
                .addOnSuccessListener { documents ->
                    followList.clear()
                    for (document in documents) {
                        val user = document.toObject(User::class.java)
                        followList.add(user)
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Error fetching follows: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchDuels() {
        firestore.collection("photo_duels")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), "Error fetching duels", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                duelList.clear()
                for (document in snapshots!!) {
                    val duel = document.toObject(PhotoDuel::class.java)
                    duel.id = document.id // Assign document ID
                    duelList.add(duel)
                }
                duelAdapter.notifyDataSetChanged()
                binding.duelRv.visibility = if (duelList.isNotEmpty()) View.VISIBLE else View.GONE

                // Check and declare winners for ended duels
                for (duel in duelList) {
                    if (duel.endTime <= System.currentTimeMillis() && duel.winner == null) {
                        declareWinner(duel)
                    }
                }
            }
    }

    private fun declareWinner(duel: PhotoDuel) {
        val duelRef = firestore.collection("photo_duels").document(duel.id)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(duelRef)
            val votes1 = snapshot.getLong("votesImage1") ?: 0
            val votes2 = snapshot.getLong("votesImage2") ?: 0
            val winner = when {
                votes1 > votes2 -> "image1"
                votes2 > votes1 -> "image2"
                else -> "draw"
            }
            transaction.update(duelRef, "winner", winner)
        }.addOnSuccessListener {
            Toast.makeText(requireContext(), "Winner declared for duel ${duel.id}", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Error declaring winner: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchPosts() {
        val currentTime = System.currentTimeMillis()

        firestore.collection(POST)
            .get()
            .addOnSuccessListener { documents ->
                postList.clear()
                for (document in documents) {
                    val post = document.toObject(Post::class.java)
                    // Check if the post is scheduled for a future time
                    if (post.scheduledTime == null || post.scheduledTime!! <= currentTime) {
                        postList.add(post)
                    }
                }
                postAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error fetching posts: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up listeners if necessary
    }
}
