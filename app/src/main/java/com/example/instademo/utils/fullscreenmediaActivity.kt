package com.example.instademo

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.instademo.R

class fullscreenmediaActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var scaleFactor = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_media)

        imageView = findViewById(R.id.full_screen_image_view)
        progressBar = findViewById(R.id.progress_bar)

        // Get image URL from the intent
        val imageUrl = intent.getStringExtra("image_url")

        if (imageUrl != null) {
            loadImage(imageUrl)
        } else {
            Toast.makeText(this, "No image URL provided", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
        }

        // Set up the scale gesture detector for pinch-to-zoom
        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())
    }

    private fun loadImage(imageUrl: String) {
        imageView.visibility = View.VISIBLE
//        Toast.makeText(this, "Image Link: $imageUrl", Toast.LENGTH_SHORT).show()

        // Load image using Glide
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.ic_error)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar.visibility = View.GONE
                    return false
                }

            })
            .into(imageView)

        // Close the activity when the image is clicked
        imageView.setOnClickListener {
            finish()
        }
    }

    // Override the touch event to detect pinch gestures
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        return true
    }

    // Inner class to handle scale gesture events (pinch-to-zoom)
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f)) // Restrict zoom level between 0.1x to 5x
            imageView.scaleX = scaleFactor
            imageView.scaleY = scaleFactor
            return true
        }
    }
}
