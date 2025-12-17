package com.example.itsupportapp  // make sure this matches your app package

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIME: Long = 3000 // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash) // we'll create this layout next

        // Move to WelcomeScreenActivity after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, WelcomeScreenActivity::class.java))
            finish() // close SplashActivity
        }, SPLASH_TIME)
    }
}
