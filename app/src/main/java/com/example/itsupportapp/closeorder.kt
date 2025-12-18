package com.example.itsupportapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class CloseOrderActivity : AppCompatActivity() {

    private lateinit var edtRequestNumber: EditText
    private lateinit var spinnerStatus: Spinner
    private lateinit var edtComment: EditText
    private lateinit var btnCloseWorkOrder: Button

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_closeorder)

        // Initialize views
        edtRequestNumber = findViewById(R.id.edtRequestNumber)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        edtComment = findViewById(R.id.edtComment)
        btnCloseWorkOrder = findViewById(R.id.btnCloseWorkOrder)

        // Initialize database helper
        dbHelper = DatabaseHelper(this)

        // Setup status spinner
        val statuses = listOf("Completed", "Pending", "Rejected", "In Progress")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statuses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = adapter

        // Handle close button click
        btnCloseWorkOrder.setOnClickListener {
            val requestNumber = edtRequestNumber.text.toString().trim()
            val status = spinnerStatus.selectedItem.toString()
            val comment = edtComment.text.toString().trim()

            if (requestNumber.isEmpty()) {
                Toast.makeText(this, "Please enter a Request Order Number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Call the function in DatabaseHelper
            val success = dbHelper.closeWorkOrder(
                issueId = requestNumber,
                status = status,
                comment = if (comment.isEmpty()) null else comment
            )

            if (success) {
                Toast.makeText(this, "Work Order updated successfully!", Toast.LENGTH_SHORT).show()
                edtRequestNumber.text.clear()
                edtComment.text.clear()
                spinnerStatus.setSelection(0)
            } else {
                Toast.makeText(this, "Failed to update Work Order. Issue with ID $requestNumber does not exist.", Toast.LENGTH_SHORT).show()
            }
        }
    }


}
