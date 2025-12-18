package com.example.itsupportapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

class HomeActivity : AppCompatActivity() {

    private lateinit var rvIssues: RecyclerView
    private lateinit var edtSearch: EditText
    private lateinit var btnFilter: ImageButton

    private lateinit var actionHome: LinearLayout
    private lateinit var actionAbout: LinearLayout
    private lateinit var actionClose: LinearLayout
    private lateinit var actionHelp: LinearLayout

    private lateinit var issueAdapter: IssueAdapter
    private var issueList: MutableList<IssueModel> = mutableListOf()
    private var filteredList: MutableList<IssueModel> = mutableListOf()

    private lateinit var dbHelper: DatabaseHelper
    private var username: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        username = intent.getStringExtra("username") ?: ""

        if (username.isEmpty()) {
            Toast.makeText(this, "No username found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // -------------------------------
        // Ensure only lecturers can access
        // -------------------------------
        dbHelper = DatabaseHelper(this)
        val role = dbHelper.getUserRole(username)
        if (role == null || !role.equals("lecturer", ignoreCase = true)) {
            Toast.makeText(this, "Access denied. Lecturers only.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        initViews()
        setupToolbar()
        setupRecycler()
        loadIssuesFromDB()
        setupSearch()
        setupBottomMenu()
        highlightSelectedButton(actionHome) // Home is selected by default
    }

    private fun initViews() {
        rvIssues = findViewById(R.id.rvIssues)
        edtSearch = findViewById(R.id.edtSearch)
        btnFilter = findViewById(R.id.btnFilter)

        actionHome = findViewById(R.id.actionHome)
        actionAbout = findViewById(R.id.actionAbout)
        actionClose = findViewById(R.id.actionClose)
        actionHelp = findViewById(R.id.actionupload)
    }

    private fun setupToolbar() {
        val toolbar: MaterialToolbar = findViewById(R.id.topAppBar)
        toolbar.setNavigationOnClickListener {
            Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecycler() {
        issueAdapter = IssueAdapter(filteredList)
        rvIssues.layoutManager = LinearLayoutManager(this)
        rvIssues.adapter = issueAdapter
    }

    private fun loadIssuesFromDB() {
        val allIssues = dbHelper.getAllIssues()
        // Lecturers see all issues
        val userIssues = allIssues

        issueList.clear()
        issueList.addAll(userIssues)
        filteredList.clear()
        filteredList.addAll(issueList)
        issueAdapter.notifyDataSetChanged()
    }

    private fun setupSearch() {
        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterIssues(s.toString())
            }
        })

        btnFilter.setOnClickListener {
            Toast.makeText(this, "Filter clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterIssues(query: String) {
        filteredList.clear()
        filteredList.addAll(issueList.filter {
            it.description.contains(query, ignoreCase = true) ||
                    it.sender.contains(query, ignoreCase = true)
        })
        issueAdapter.notifyDataSetChanged()
    }

    private fun setupBottomMenu() {
        actionHome.setOnClickListener {
            Toast.makeText(this, "Already on Home", Toast.LENGTH_SHORT).show()
            highlightSelectedButton(actionHome)
        }

        actionAbout.setOnClickListener {
            startActivity(Intent(this, AboutAppPageActivity::class.java))
            highlightSelectedButton(actionAbout)
        }

        actionClose.setOnClickListener {
            startActivity(Intent(this, CloseOrderActivity::class.java))
            highlightSelectedButton(actionClose)
        }

        actionHelp.setOnClickListener {
            startActivity(Intent(this, HelpTutorials::class.java))
            highlightSelectedButton(actionHelp)
        }
    }

    private fun highlightSelectedButton(selected: LinearLayout) {
        val buttons = listOf(actionHome, actionAbout, actionClose, actionHelp)
        for (btn in buttons) {
            val tv = btn.getChildAt(1) as TextView
            tv.setTextColor(Color.BLACK)
        }
        val selectedTv = selected.getChildAt(1) as TextView
        selectedTv.setTextColor(Color.parseColor("#6200EE"))
    }

    // -------------------- Inner Classes --------------------

    // Model class
    data class IssueModel(
        val id: String,
        val title: String,
        val description: String,
        val sender: String,
        val phone: String,
        val status: String,
        val date: String? = null
    )

    // Adapter class
    inner class IssueAdapter(private var issues: MutableList<IssueModel>) :
        RecyclerView.Adapter<IssueAdapter.IssueViewHolder>() {

        inner class IssueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvIssueId: TextView = itemView.findViewById(R.id.tvIssueId)
            val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
            val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
            val tvPhoneNumber: TextView = itemView.findViewById(R.id.tvphone_no)
            val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
            val tvClosureDate: TextView = itemView.findViewById(R.id.tvClosureDate)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssueViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.activity_item_issue_row, parent, false)
            return IssueViewHolder(view)
        }

        override fun getItemCount(): Int = issues.size

        override fun onBindViewHolder(holder: IssueViewHolder, position: Int) {
            val issue = issues[position]
            holder.tvIssueId.text = issue.id
            holder.tvDescription.text = "${issue.title}: ${issue.description}"  // show title + description
            holder.tvUsername.text = issue.sender
            holder.tvPhoneNumber.text = issue.phone
            holder.tvStatus.text = issue.status
            holder.tvClosureDate.text = issue.date
        }

        fun updateData(newIssues: List<IssueModel>) {
            issues.clear()
            issues.addAll(newIssues)
            notifyDataSetChanged()
        }
    }

        }


