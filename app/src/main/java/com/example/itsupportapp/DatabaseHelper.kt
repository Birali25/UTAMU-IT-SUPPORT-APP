package com.example.itsupportapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

// ---------------------------
// Database Helper
// ---------------------------
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "itsupport.db"
        private const val DATABASE_VERSION = 1

        // ------------------ USER TABLE ------------------
        private const val TABLE_USER = "User"
        private const val COL_USER_ID = "user_id"
        private const val COL_USERNAME = "username"
        private const val COL_PASSWORD = "password"
        private const val COL_EMAIL = "email"
        private const val COL_PHONE = "phone_number"
        private const val COL_ROLE = "role"

        // ------------------ ISSUE TABLE ------------------
        private const val TABLE_ISSUE = "Issue"
        private const val COL_ISSUE_ID = "issue_id"
        private const val COL_ISSUE_TITLE = "issue_title"
        private const val COL_ISSUE_DESC = "description"
        private const val COL_ISSUE_USERNAME = "username"
        private const val COL_ISSUE_STATUS = "status"
        private const val COL_ISSUE_DATE = "submission_date"

        // ------------------ CLOSE ORDER TABLE ------------------
        private const val TABLE_CLOSE_ORDER = "CloseOrder"
        private const val COL_CLOSE_ID = "close_id"
        private const val COL_CLOSE_ISSUE_ID = "issue_id"
        private const val COL_CLOSE_COMMENT = "comment"
        private const val COL_CLOSE_DATE = "closure_date"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            """
            CREATE TABLE $TABLE_USER (
                $COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USERNAME TEXT NOT NULL UNIQUE,
                $COL_PASSWORD TEXT NOT NULL,
                $COL_EMAIL TEXT NOT NULL UNIQUE,
                $COL_PHONE TEXT,
                $COL_ROLE TEXT NOT NULL CHECK ($COL_ROLE IN ('student','lecturer'))
            )
            """.trimIndent()
        )

        db?.execSQL(
            """
            CREATE TABLE $TABLE_ISSUE (
                $COL_ISSUE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_ISSUE_TITLE TEXT NOT NULL,
                $COL_ISSUE_DESC TEXT NOT NULL,
                $COL_ISSUE_USERNAME TEXT NOT NULL,
                $COL_ISSUE_STATUS TEXT NOT NULL,
                $COL_ISSUE_DATE TEXT NOT NULL
            )
            """.trimIndent()
        )

        db?.execSQL(
            """
            CREATE TABLE $TABLE_CLOSE_ORDER (
                $COL_CLOSE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CLOSE_ISSUE_ID INTEGER NOT NULL,
                $COL_CLOSE_COMMENT TEXT,
                $COL_CLOSE_DATE TEXT NOT NULL
            )
            """.trimIndent()
        )
    }


    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CLOSE_ORDER")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ISSUE")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        onCreate(db)
    }

    // ------------------- USER FUNCTIONS -------------------
    fun insertUser(username: String, password: String, email: String, phone: String, role: String): Boolean {
        if (checkUserExists(username, email)) return false

        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USERNAME, username)
            put(COL_PASSWORD, password)
            put(COL_EMAIL, email)
            put(COL_PHONE, phone)
            put(COL_ROLE, role.lowercase()) // convert role to lowercase
        }

        val result = db.insert(TABLE_USER, null, values)
        db.close()
        return result != -1L
    }


    fun checkUserLogin(username: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_USER_ID FROM $TABLE_USER WHERE $COL_USERNAME=? AND $COL_PASSWORD=?",
            arrayOf(username, password)
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        db.close()
        return exists
    }

    fun getUserRole(username: String): String? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_ROLE FROM $TABLE_USER WHERE $COL_USERNAME=?",
            arrayOf(username)
        )
        val role = if (cursor.moveToFirst()) cursor.getString(0) else null
        cursor.close()
        db.close()
        return role
    }

    fun getUserPhone(username: String): String? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_PHONE FROM $TABLE_USER WHERE $COL_USERNAME=?",
            arrayOf(username)
        )
        val phone = if (cursor.moveToFirst()) cursor.getString(0) else null
        cursor.close()
        db.close()
        return phone
    }

    fun checkUserExists(username: String?, email: String?): Boolean {
        if (username.isNullOrBlank() && email.isNullOrBlank()) return false
        val db = readableDatabase
        val args = mutableListOf<String>()
        val query = StringBuilder("SELECT $COL_USER_ID FROM $TABLE_USER WHERE 1=0")

        username?.takeIf { it.isNotBlank() }?.let {
            query.append(" OR LOWER($COL_USERNAME) = LOWER(?)")
            args.add(it)
        }
        email?.takeIf { it.isNotBlank() }?.let {
            query.append(" OR LOWER($COL_EMAIL) = LOWER(?)")
            args.add(it)
        }

        val cursor = db.rawQuery(query.toString(), args.toTypedArray())
        val exists = cursor.moveToFirst()
        cursor.close()
        db.close()
        return exists
    }

    // ------------------- ISSUE FUNCTIONS -------------------
    fun addIssue(issueTitle: String, description: String, username: String, status: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ISSUE_TITLE, issueTitle)
            put(COL_ISSUE_DESC, "$issueTitle: $description")
            put(COL_ISSUE_USERNAME, username)
            put(COL_ISSUE_STATUS, status)
            put(COL_ISSUE_DATE, getCurrentDate())
        }
        val result = db.insert(TABLE_ISSUE, null, values)
        db.close()
        return result != -1L
    }

    fun updateIssueStatus(issueId: String, status: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply { put(COL_ISSUE_STATUS, status) }
        val rows = db.update(TABLE_ISSUE, values, "$COL_ISSUE_ID=?", arrayOf(issueId))
        db.close()
        return rows > 0
    }

    // ------------------- CLOSE ORDER FUNCTIONS -------------------
    fun closeWorkOrder(issueId: String, status: String, comment: String?): Boolean {
        val db = writableDatabase
        var success = false

        // Update Issue status
        val rowsUpdated = db.update(
            TABLE_ISSUE,
            ContentValues().apply { put(COL_ISSUE_STATUS, status) },
            "$COL_ISSUE_ID=?",
            arrayOf(issueId)
        )

        if (rowsUpdated > 0) {
            val values = ContentValues().apply {
                put(COL_CLOSE_ISSUE_ID, issueId)
                put(COL_CLOSE_COMMENT, comment)
                put(COL_CLOSE_DATE, getCurrentDate())
            }
            val result = db.insert(TABLE_CLOSE_ORDER, null, values)
            success = result != -1L
        }
        db.close()
        return success
    }

    fun getStudentIssues(username: String): MutableList<Issuestudent> {
        val issueList = mutableListOf<Issuestudent>()
        val db = readableDatabase

        val query = """
            SELECT i.$COL_ISSUE_DESC, i.$COL_ISSUE_STATUS, i.$COL_ISSUE_DATE, c.$COL_CLOSE_DATE
            FROM $TABLE_ISSUE i
            LEFT JOIN $TABLE_CLOSE_ORDER c ON i.$COL_ISSUE_ID = c.$COL_CLOSE_ISSUE_ID
            WHERE i.$COL_ISSUE_USERNAME=?
            ORDER BY i.$COL_ISSUE_DATE DESC
        """.trimIndent()

        db.rawQuery(query, arrayOf(username)).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val description = cursor.getString(cursor.getColumnIndexOrThrow(COL_ISSUE_DESC))
                    val status = cursor.getString(cursor.getColumnIndexOrThrow(COL_ISSUE_STATUS))
                    val submissionDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_ISSUE_DATE))
                    val closureDate = cursor.getColumnIndex("closure_date").takeIf { it != -1 }?.let { cursor.getString(it) }
                    val finalDate = closureDate ?: submissionDate

                    issueList.add(Issuestudent(
                        description = description,
                        status = status,
                        date = finalDate,
                        submissionDate = submissionDate
                    ))
                } while (cursor.moveToNext())
            }
        }

        db.close()
        return issueList
    }

    fun getAllIssues(): List<HomeActivity.IssueModel> {
        val issues = mutableListOf<HomeActivity.IssueModel>()
        val db = readableDatabase

        val query = """
        SELECT 
            i.$COL_ISSUE_ID,
            i.$COL_ISSUE_TITLE,
            i.$COL_ISSUE_DESC,
            u.$COL_USERNAME,
            u.$COL_PHONE,
            i.$COL_ISSUE_STATUS,
            i.$COL_ISSUE_DATE,
            c.$COL_CLOSE_DATE AS closure_date
        FROM $TABLE_ISSUE i
        INNER JOIN $TABLE_USER u ON i.$COL_ISSUE_USERNAME = u.$COL_USERNAME
        LEFT JOIN $TABLE_CLOSE_ORDER c ON i.$COL_ISSUE_ID = c.$COL_CLOSE_ISSUE_ID
        ORDER BY i.$COL_ISSUE_DATE DESC
    """.trimIndent()

        db.rawQuery(query, null).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ISSUE_ID)).toString()
                    val title = cursor.getString(cursor.getColumnIndexOrThrow(COL_ISSUE_TITLE))
                    val desc = cursor.getString(cursor.getColumnIndexOrThrow(COL_ISSUE_DESC))
                    val sender = cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME))
                    val phone = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE))
                    val status = cursor.getString(cursor.getColumnIndexOrThrow(COL_ISSUE_STATUS))
                    val submissionDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_ISSUE_DATE))
                    val closureDate = cursor.getColumnIndex("closure_date").takeIf { it != -1 }?.let { cursor.getString(it) }
                    val finalDate = closureDate ?: submissionDate

                    issues.add(HomeActivity.IssueModel(
                        id = id,
                        title = title,           // Issue title
                        description = desc,      // Issue description
                        sender = sender,
                        phone = phone,
                        status = status,
                        date = finalDate
                    ))
                } while (cursor.moveToNext())
            }
        }

        db.close()
        return issues
    }


    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }
}
