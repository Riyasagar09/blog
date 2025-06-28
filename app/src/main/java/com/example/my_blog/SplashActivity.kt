package com.example.my_blog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val splashDuration = 2000L // 2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val prefs = getSharedPreferences("user_data", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        val username = prefs.getString("name", "Blogger")

        Handler(Looper.getMainLooper()).postDelayed({
            if (isLoggedIn) {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("username", username)
                startActivity(intent)
            } else {
                startActivity(Intent(this, WelcomeActivity::class.java))
            }
            finish()
        }, splashDuration)
    }
}
