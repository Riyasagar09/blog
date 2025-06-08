package com.example.my_blog

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity



import com.example.my_blog.databinding.ActivityWelcomeBinding
import com.example.my_blog.loginActivity // Adjust the import path as per your package

class WelcomeActivity : AppCompatActivity() {

    private val binding: ActivityWelcomeBinding by lazy {
        ActivityWelcomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            val intent = Intent(this, loginActivity::class.java)
            intent.putExtra("action", "login")
            startActivity(intent)
        }

        binding.button2.setOnClickListener {
            val intent = Intent(this, loginActivity::class.java)
            intent.putExtra("action", "register")
            startActivity(intent)
        }
    }
}
