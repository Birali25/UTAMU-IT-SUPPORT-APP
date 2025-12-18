package com.example.itsupportapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ReportIssueActivity : AppCompatActivity() {

    private lateinit var edtIssueDescription: EditText
    private lateinit var spinnerIssue: Spinner
    private lateinit var btnSubmitIssue: Button

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reportissue)

        // Get logged-in username from previous page
        username = intent.getStringExtra("username") ?: ""
        if (username.isEmpty()) {
            Toast.makeText(this, "User not found. Please log in again.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Initialize views
        edtIssueDescription = findViewById(R.id.edtIssueDescription)
        spinnerIssue = findViewById(R.id.spinner_issue)
        btnSubmitIssue = findViewById(R.id.btnSubmitIssue)

        dbHelper = DatabaseHelper(this)

        // List of issues to select from
        val issues = listOf(
            "Account locked", "Forgot portal password", "Cannot upload assignment",
            "Cannot receive school email", "Notes not opening", "E-learning crashing",
            "Quiz not opening", "E-learning password reset", "Unable to login",
            "Cannot join online class", "Grade not appearing", "Missing coursework marks",
            "GPA not correct", "Wrong grade recorded"
        )

        spinnerIssue.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            issues
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // Handle submit button click
        btnSubmitIssue.setOnClickListener {

            val descriptionText = edtIssueDescription.text.toString().trim()
            val selectedIssueTitle = spinnerIssue.selectedItem.toString()

            // Check for empty description
            if (descriptionText.isEmpty()) {
                Toast.makeText(this, "Please describe the issue", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save issue in database
            val success = dbHelper.addIssue(
                issueTitle = selectedIssueTitle,
                description = descriptionText,
                username = username,
                status = "Pending"  // default status
            )

            if (success) {
                Toast.makeText(this, "Issue submitted successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, StudentHomeActivity::class.java)
                intent.putExtra("username", username)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Failed to submit issue. Try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
