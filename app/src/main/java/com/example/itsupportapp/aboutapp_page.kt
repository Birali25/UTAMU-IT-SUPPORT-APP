package com.example.itsupportapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AboutAppPageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aboutapp_page) // Link to your XML layout

        // Access views if you want to manipulate them programmatically
        val ivAppLogo = findViewById<ImageView>(R.id.ivAppLogo)
        val tvPageTitle = findViewById<TextView>(R.id.tvPageTitle)
        val tvDescription = findViewById<TextView>(R.id.tvDescription)

        // Set text dynamically (optional)
        tvPageTitle.text = "UTAMU IT Support App"
        tvDescription.text = "This app helps students and lecturers report, track, and resolve IT-related issues at UTAMU."

        // Enable back button in the top-left corner
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "About App" // Optional: change action bar title
    }

    // Handle back button click
    override fun onSupportNavigateUp(): Boolean {
        finish() // Close this activity and return to previous
        return true
    }
}
