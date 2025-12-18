package com.example.itsupportapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// ---------------------------
// Student Issue Model
// ---------------------------
data class Issuestudent(
    val description: String,
    val status: String,
    val date: String,
    val submissionDate: String
)

// ---------------------------
// RecyclerView Adapter
// ---------------------------
class StudentIssueAdapter(private val issues: List<Issuestudent>) :
    RecyclerView.Adapter<StudentIssueAdapter.IssueViewHolder>() {

    class IssueViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val tvDescription = itemView.findViewById<android.widget.TextView>(R.id.tvDescription)
        val tvStatus = itemView.findViewById<android.widget.TextView>(R.id.tvStatus)
        val tvDate = itemView.findViewById<android.widget.TextView>(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): IssueViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_student_issue, parent, false)
        return IssueViewHolder(view)
    }

    override fun onBindViewHolder(holder: IssueViewHolder, position: Int) {
        val issue = issues[position]
        holder.tvDescription.text = issue.description
        holder.tvStatus.text = "Status: ${issue.status}"
        holder.tvDate.text = "Date: ${issue.date}"
    }

    override fun getItemCount(): Int = issues.size
}

// ---------------------------
// Student Home Activity
// ---------------------------
class StudentHomeActivity : AppCompatActivity() {

    private lateinit var rvIssues: RecyclerView
    private lateinit var btnAboutUs: Button
    private lateinit var btnReportIssue: Button
    private lateinit var btnHelpTutorials: Button

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var issueAdapter: StudentIssueAdapter

    private var username: String = ""
    private val issueList: MutableList<Issuestudent> = mutableListOf()
    private val filteredList: MutableList<Issuestudent> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Make sure this layout matches your main activity XML
        setContentView(R.layout.activity_student_homepage)

        // Get username from login intent
        username = intent.getStringExtra("username") ?: ""
        if (username.isEmpty()) {
            Toast.makeText(this, "No username found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupRecycler()
        loadStudentIssues()
        setupBottomMenu()
    }

    private fun initViews() {
        rvIssues = findViewById(R.id.issuesRecyclerView)
        btnAboutUs = findViewById(R.id.aboutUsButton)
        btnReportIssue = findViewById(R.id.reportIssueButton)
        btnHelpTutorials = findViewById(R.id.helpTutorialsButton)

        dbHelper = DatabaseHelper(this)
    }

    private fun setupRecycler() {
        issueAdapter = StudentIssueAdapter(filteredList)
        rvIssues.layoutManager = LinearLayoutManager(this)
        rvIssues.adapter = issueAdapter
    }
    private fun loadStudentIssues() {
        val issuesFromDb = dbHelper.getStudentIssues(username)  // <-- Call your function

        issueList.clear()
        filteredList.clear()
        issueList.addAll(issuesFromDb)
        filteredList.addAll(issueList)

        issueAdapter.notifyDataSetChanged()  // Refresh RecyclerView
    }



    private fun setupBottomMenu() {
        btnAboutUs.setOnClickListener {
            startActivity(Intent(this, AboutAppPageActivity::class.java))
        }
        btnReportIssue.setOnClickListener {
            val intent = Intent(this, ReportIssueActivity::class.java)
            intent.putExtra("username", username)  // pass logged-in user
            startActivity(intent)
        }

        btnHelpTutorials.setOnClickListener {
            startActivity(Intent(this, HelpTutorials::class.java))
        }
    }
}
