// File: com/example/instademo/adapter/PostDuelAdapter.kt

package com.example.instademo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.instademo.R
import com.example.instademo.model.PhotoDuel
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class PostDuelAdapter(
    private val context: Context,
    private val duelList: List<PhotoDuel>
) : RecyclerView.Adapter<PostDuelAdapter.DuelViewHolder>() {

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DuelViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_post_duel, parent, false)
        return DuelViewHolder(view)
    }

    override fun onBindViewHolder(holder: DuelViewHolder, position: Int) {
        val duel = duelList[position]
        holder.bind(duel)
    }

    override fun getItemCount(): Int = duelList.size

    inner class DuelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image1: ImageView = itemView.findViewById(R.id.image1)
        private val image2: ImageView = itemView.findViewById(R.id.image2)
        private val caption1: TextView = itemView.findViewById(R.id.caption1)
        private val caption2: TextView = itemView.findViewById(R.id.caption2)
        private val vote1: Button = itemView.findViewById(R.id.vote1)
        private val vote2: Button = itemView.findViewById(R.id.vote2)
        private val votes1: TextView = itemView.findViewById(R.id.votes1)
        private val votes2: TextView = itemView.findViewById(R.id.votes2)
        private val winnerText: TextView = itemView.findViewById(R.id.winnerText)

        fun bind(duel: PhotoDuel) {
            // Load images
            Picasso.get().load(duel.image1Url).into(image1)
            Picasso.get().load(duel.image2Url).into(image2)

            // Set captions
            caption1.text = duel.caption1
            caption2.text = duel.caption2

            // Set vote counts
            votes1.text = "Votes: ${duel.votesImage1}"
            votes2.text = "Votes: ${duel.votesImage2}"

            // Check if duel has ended
            if (duel.winner != null) {
                displayWinner(duel.winner!!)
                disableVoting()
            } else {
                winnerText.text = ""
                enableVoting()

                // Set click listeners for voting
                vote1.setOnClickListener {
                    vote(duel.id, "image1")
                }

                vote2.setOnClickListener {
                    vote(duel.id, "image2")
                }
            }
        }

        private fun displayWinner(winner: String) {
            when (winner) {
                "image1" -> winnerText.text = "Winner: Image 1"
                "image2" -> winnerText.text = "Winner: Image 2"
                "draw" -> winnerText.text = "It's a Draw!"
                else -> winnerText.text = ""
            }
        }

        private fun disableVoting() {
            vote1.isEnabled = false
            vote2.isEnabled = false
        }

        private fun enableVoting() {
            vote1.isEnabled = true
            vote2.isEnabled = true
        }

        private fun vote(duelId: String, image: String) {
            val duelRef = firestore.collection("photo_duels").document(duelId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(duelRef)
                val currentVotesImage1 = snapshot.getLong("votesImage1") ?: 0
                val currentVotesImage2 = snapshot.getLong("votesImage2") ?: 0

                when (image) {
                    "image1" -> transaction.update(duelRef, "votesImage1", currentVotesImage1 + 1)
                    "image2" -> transaction.update(duelRef, "votesImage2", currentVotesImage2 + 1)
                    else -> {throw error("msg")}
                }
            }.addOnSuccessListener {
                Toast.makeText(context, "Voted for $image", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to vote: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
