package com.example.instademo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.instademo.databinding.ActivityAboutUsBinding

class Aboutus : AppCompatActivity() {
    private lateinit var binding: ActivityAboutUsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutUsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvEmail.setOnClickListener{
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:") // Only email apps should handle this
                putExtra(Intent.EXTRA_EMAIL, arrayOf("aadarshchaurasia45@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Subject of your email")
                putExtra(Intent.EXTRA_TEXT, "Body of your email")
            }
            if (emailIntent.resolveActivity(packageManager) != null) {
                startActivity(emailIntent)
            }

        }
        binding.tvGithub.setOnClickListener {
            val githubUrl = "https://github.com/Aadarsh45"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(githubUrl)
            startActivity(intent)

        }

    }
}