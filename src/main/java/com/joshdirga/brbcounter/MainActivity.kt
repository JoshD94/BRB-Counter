package com.joshdirga.brbcounter

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize variables
        var button = findViewById<Button>(R.id.button)
        var startingAmount = findViewById<EditText>(R.id.editTextNumberDecimal1)
        // Set initial amount to 400
        startingAmount.setText("400")
        var currentAmount = findViewById<EditText>(R.id.editTextNumberDecimal2)
        // Set initial amount to 400
        currentAmount.setText("400")
        var percentSemester = findViewById<TextView>(R.id.textView4)
        var percentBrbs = findViewById<TextView>(R.id.textView5)
        var weeklyUsage = findViewById<TextView>(R.id.textView6)
        var dailyUsage = findViewById<TextView>(R.id.textView7)
        var comments = findViewById<TextView>(R.id.textView8)
        // Get today's date
        var dateToday = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.now()
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        var startDate = LocalDate.now()
        var endDate = LocalDate.now()
        // Set spinner contents
        var semester = findViewById<Spinner>(R.id.spinner1)
        val semesters = arrayOf<String>("Fall 2023", "Spring 2024")
        // Automatically set selection based on year
        if (semester != null) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, semesters)
            semester.adapter = adapter
            // Fall
            startDate = LocalDate.of(2023, 8, 21)
            endDate = LocalDate.of(2023, 12, 4)
            if (dateToday.year == 2024) {
                // Spring
                semester.setSelection(1)
            }
        }

        button.setOnClickListener() {
            // Hide keyboard
            semester.hideKeyboard()
            // Check for input
            if (currentAmount.text.toString() != "" && startingAmount.text.toString() != "") {
                if (semester.selectedItem.toString() == "Spring 2024") {
                    // Spring
                    startDate = LocalDate.of(2024, 1, 22)
                    endDate = LocalDate.of(2024, 5, 7)
                } else {
                    // Fall
                    startDate = LocalDate.of(2023, 8, 21)
                    endDate = LocalDate.of(2023, 12, 4)
                }
                // Calculate durations
                var totalDuration = daysDiff(startDate.toString(), endDate.toString())
                var leftOverDuration = daysDiff(dateToday.toString(), endDate.toString())
                // Calculate percent of semester based on dates
                var percentSem = (daysDiff(
                    dateToday.toString(),
                    startDate.toString()
                ).toDouble() / totalDuration * 100.0).toBigDecimal().setScale(1, RoundingMode.UP)
                // If today's date is before start date then do not take difference in time
                if (!dateToday.isAfter(startDate)) {
                    percentSem = BigDecimal(0)
                    leftOverDuration = totalDuration
                }
                // Calculate percent used
                var percentUsed =
                    ((startingAmount.getText().toString().toDouble() - currentAmount.getText()
                        .toString().toDouble()) / startingAmount.getText().toString()
                        .toDouble() * 100).toBigDecimal().setScale(1, RoundingMode.UP)
                // Calculate amount used
                var used = startingAmount.getText().toString().toDouble() - currentAmount.getText()
                    .toString().toDouble()
                var leftOver = currentAmount.getText().toString().toDouble()
                var weekly =
                    (leftOver / (leftOverDuration / 7)).toBigDecimal().setScale(2, RoundingMode.UP)
                var daily =
                    (leftOver / leftOverDuration).toBigDecimal().setScale(2, RoundingMode.UP)

                // Assign text to TextViews
                percentSemester.text = "Semester completion: " + percentSem + "%"
                percentBrbs.text = "BRB's used: " + percentUsed + "%" + " ($" + used.toBigDecimal()
                    .setScale(1, RoundingMode.UP) + ")"
                weeklyUsage.text = "Projected weekly usage: $" + weekly
                dailyUsage.text = "Projected daily usage: $" + daily

                // Generate comment
                if (percentUsed > percentSem) {
                    comments.text =
                        "You are spending more BRB's than the suggested pace! Try hold back on spendings :D"
                } else if (percentUsed < percentSem) {
                    if (semester.selectedItem.toString() == "Spring 2024") {
                        comments.text =
                            "You are spending less BRB's than the suggested pace! As it is the Spring semester, you might want to spend more because your leftover BRB's will be gone once Spring semester ends!"
                    } else {
                        comments.text =
                            "You are spending less BRB's than the suggested pace! As it is the Fall semester, your BRB's will transfer to next Spring, but you can always treat yourself to some extra cookies after classes :D"
                    }
                } else {
                    comments.text = "Wow, you are spending the exact amount per day! Keep it up!"
                }
            } else {
                // If fields not filled show error
                showIncompleteFieldsAlert()
            }
        }
    }

    /*
    Displays an alert asking the user to fill fields
     */
    fun showIncompleteFieldsAlert() {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)
        alertDialog.setTitle("Incomplete Fields")//for set Title
        alertDialog.setMessage("Please fill in all fields")// for Message
//        alertDialog.setIcon(Your icon path) // for alert icon
        alertDialog.setPositiveButton("Continue") { dialog, id ->
            // set your desired action here.
        }
        val alert = alertDialog.create()
        alert.setCanceledOnTouchOutside(true)
        alert.show()
    }

    /*
    Calculates the difference in days between two dates
    Returns an integer value
     */
    open fun daysDiff(date1: String, date2: String): Int {
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd") //Define input date format here
        val formatedDate1 = dateFormatter.parse(date1)  //formated  date1
        val formatedDate2 = dateFormatter.parse(date2)  //formated date2
        val millionSeconds = formatedDate2.time - formatedDate1.time
        return abs(TimeUnit.MILLISECONDS.toDays(millionSeconds).toInt())
    }

    /*
    Hides the keyboard
    Can be used on an view element eg. EditText, TextView, etc.
     */
    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}
