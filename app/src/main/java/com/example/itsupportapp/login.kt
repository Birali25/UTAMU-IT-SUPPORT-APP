package com.example.itsupportapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var edtUsername: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var txtSignUp: TextView

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        edtUsername = findViewById(R.id.edtUsername)
        edtPassword = findViewById(R.id.edtPassword)
        btnLogin = findViewById(R.id.btnLogin)
        txtSignUp = findViewById(R.id.txtSignUp)

        dbHelper = DatabaseHelper(this)

        btnLogin.setOnClickListener {
            val username = edtUsername.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate login
            val validLogin = dbHelper.checkUserLogin(username, password)
            if (!validLogin) {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get the user role
            val role = dbHelper.getUserRole(username)
            if (role == null) {
                Toast.makeText(this, "User role not found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Navigate based on lowercase role
            when (role.lowercase()) {
                "student" -> {
                    val intent = Intent(this, StudentHomeActivity::class.java)
                    intent.putExtra("username", username)
                    startActivity(intent)
                    finish()
                }

                "lecturer" -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.putExtra("username", username)
                    startActivity(intent)
                    finish()
                }

                else -> {
                    Toast.makeText(this, "Unknown role: $role", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Navigate to sign-up
        txtSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}
