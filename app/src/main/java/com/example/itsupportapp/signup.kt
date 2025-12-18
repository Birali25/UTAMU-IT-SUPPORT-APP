package com.example.itsupportapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {

    private lateinit var edtUsername: EditText
    private lateinit var edtPassword: EditText
    private lateinit var edtPhone: EditText
    private lateinit var edtEmail: EditText
    private lateinit var rgUserRole: RadioGroup
    private lateinit var rbStudent: RadioButton
    private lateinit var rbLecturer: RadioButton
    private lateinit var btnSignUp: Button
    private lateinit var txtLogin: TextView

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize views
        edtUsername = findViewById(R.id.edtUsername)
        edtPassword = findViewById(R.id.edtPassword)
        edtPhone = findViewById(R.id.edtPhone)
        edtEmail = findViewById(R.id.edtemail)
        rgUserRole = findViewById(R.id.rgUserRole)
        rbStudent = findViewById(R.id.rbStudent)
        rbLecturer = findViewById(R.id.rbLecturer)
        btnSignUp = findViewById(R.id.btnsignUp)
        txtLogin = findViewById(R.id.txtlogin)

        // Database helper
        dbHelper = DatabaseHelper(this)

        // When user clicks SIGN UP
        btnSignUp.setOnClickListener {
            val username = edtUsername.text.toString().trim()
            val password = edtPassword.text.toString().trim()
            val phone = edtPhone.text.toString().trim()
            val email = edtEmail.text.toString().trim()

            // Validate empty fields
            if (username.isEmpty() || password.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate role selection
            val role = when (rgUserRole.checkedRadioButtonId) {
                R.id.rbStudent -> "Student"
                R.id.rbLecturer -> "Lecturer"
                else -> {
                    Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // Check existing username or email
            if (dbHelper.checkUserExists(username, email)) {
                Toast.makeText(this, "Username or Email already exists", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Insert into database
            val success = dbHelper.insertUser(username, password, email, phone, role)

            if (success) {
                Toast.makeText(this, "Sign Up Successful", Toast.LENGTH_SHORT).show()

                // Go to Login screen
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Sign Up Failed. Try Again.", Toast.LENGTH_SHORT).show()
            }
        }

        // Already have an account?
        txtLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
