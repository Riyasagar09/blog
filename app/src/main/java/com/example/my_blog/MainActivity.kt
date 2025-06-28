package com.example.my_blog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private lateinit var btnWrite: Button
    private lateinit var btnRead: Button
    private lateinit var btnTrending: Button
    private lateinit var tvWelcome: TextView
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnWrite = findViewById(R.id.btnWrite)
        btnRead = findViewById(R.id.btnRead)
        btnTrending = findViewById(R.id.btnTrending)
        tvWelcome = findViewById(R.id.tvWelcome)

        val username = intent.getStringExtra("username") ?: "Blogger"
        tvWelcome.text = "👋 Welcome, $username!"

        btnLogout = Button(this).apply {
            text = "Logout"
            setTextColor(android.graphics.Color.parseColor("#6E0550"))
            textSize = 16f
            setBackgroundResource(android.R.color.transparent)
            setPadding(0, 32, 0, 0)
        }
        (tvWelcome.parent as android.view.ViewGroup).addView(btnLogout)

        btnWrite.setOnClickListener {
            val intent = Intent(this, WriteBlogActivity::class.java)
            intent.putExtra("username", username) // pass username if needed in WriteBlogActivity
            startActivity(intent)
        }

        btnRead.setOnClickListener {
            val dbHelper = BlogDbHelper(this)
            val latestBlog = dbHelper.getLatestBlogId() // Implement this method
            if (latestBlog != null) {
                val intent = Intent(this, ReadBlogActivity::class.java)
                intent.putExtra("blog_id", latestBlog)
                startActivity(intent)
            } else {
                Toast.makeText(this, "No blogs found", Toast.LENGTH_SHORT).show()
            }
        }

        btnTrending.setOnClickListener {
            val intent = Intent(this, TrendingNewsActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            val prefs = getSharedPreferences("user_data", MODE_PRIVATE)
            prefs.edit().putBoolean("is_logged_in", false).apply()
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
