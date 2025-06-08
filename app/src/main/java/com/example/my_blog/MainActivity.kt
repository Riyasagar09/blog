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
    private lateinit var tvWelcome: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnWrite = findViewById(R.id.btnWrite)
        btnRead = findViewById(R.id.btnRead)
        tvWelcome = findViewById(R.id.tvWelcome)

        val username = intent.getStringExtra("username") ?: "Blogger"
        tvWelcome.text = "👋 Welcome, $username!"

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
    }
}
