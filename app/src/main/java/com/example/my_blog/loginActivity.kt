package com.example.my_blog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


import com.example.my_blog.databinding.ActivityLoginBinding

class loginActivity : AppCompatActivity() {

    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private lateinit var prefs: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Get SharedPreferences
        prefs = getSharedPreferences("user_data", Context.MODE_PRIVATE)

        val action: String? = intent.getStringExtra("action")

        if (action == "login") {
            setupLoginMode()
        } else if (action == "register") {
            setupRegisterMode()
        }

        // Login button logic
        binding.loginButton.setOnClickListener {
            val name = binding.editName.text.toString().trim()
            val email = binding.editEmail.text.toString().trim()
            val password = binding.editPassword.text.toString()

            if (action == "login") {
                val savedEmail = prefs.getString("email", null)
                val savedPassword = prefs.getString("password", null)
                val savedName = prefs.getString("name", "User")

                if (email == savedEmail && password == savedPassword) {
                    Toast.makeText(this, "✅ Welcome back $savedName!", Toast.LENGTH_SHORT).show()
                    prefs.edit().putBoolean("is_logged_in", true).apply()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("username", savedName)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "❌ Incorrect credentials", Toast.LENGTH_SHORT).show()
                }
            }

            if (action == "register") {
                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "⚠️ Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (!email.contains("@") || password.length < 6) {
                    Toast.makeText(this, "❌ Invalid email or weak password", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Save user data
                prefs.edit().apply {
                    putString("name", name)
                    putString("email", email)
                    putString("password", password)
                    putBoolean("is_logged_in", true)
                    apply()
                }

                Toast.makeText(this, "✅ Registration successful!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("username", name)
                startActivity(intent)
                finish()
            }
        }

        // Register button from login screen (optional)
        binding.registerButton.setOnClickListener {
            val intent = Intent(this, loginActivity::class.java)
            intent.putExtra("action", "register")
            startActivity(intent)
        }
    }

    private fun setupLoginMode() {
        binding.editName.visibility = View.GONE // No need for name on login
        binding.registerButton.visibility = View.VISIBLE
        binding.textNewHere.visibility = View.VISIBLE
        binding.loginButton.text = "Login"
    }

    private fun setupRegisterMode() {
        binding.editName.visibility = View.VISIBLE
        binding.registerButton.visibility = View.GONE
        binding.textNewHere.visibility = View.GONE
        binding.loginButton.text = "Register"
    }
}
