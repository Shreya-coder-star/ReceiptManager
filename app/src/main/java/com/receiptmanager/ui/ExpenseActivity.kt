package com.receiptmanager.ui

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.receiptmanager.R
import com.receiptmanager.db.DatabaseHelper
import com.receiptmanager.util.CategoryHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class ExpenseActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var tvTodayAmount: TextView
    private lateinit var tvMonthAmount: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var tvTodayCount: TextView
    private lateinit var tvMonthCount: TextView
    private lateinit var barChartContainer: LinearLayout
    private lateinit var categoryContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense)

        db = DatabaseHelper(this)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "Expense Summary"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)

        tvTodayAmount      = findViewById(R.id.tvTodayAmount)
        tvMonthAmount      = findViewById(R.id.tvMonthAmount)
        tvTotalAmount      = findViewById(R.id.tvTotalAmount)
        tvTodayCount       = findViewById(R.id.tvTodayCount)
        tvMonthCount       = findViewById(R.id.tvMonthCount)
        barChartContainer  = findViewById(R.id.barChartContainer)
        categoryContainer  = findViewById(R.id.categoryBreakdownContainer)

        loadData()
    }

    private fun loadData() {
        thread {
            val todayFmt  = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val monthFmt  = SimpleDateFormat("MM-yyyy", Locale.getDefault())
            val today     = todayFmt.format(Date())
            val month     = monthFmt.format(Date())

            val todayAmt  = db.getTotalAmountByDate(today)
            val monthAmt  = db.getTotalAmountByMonth(month)
            val totalAmt  = db.getTotalAmountAll()
            val todayCnt  = db.getCountByDate(today)
            val monthCnt  = db.getTotalCount()
            val weekly    = db.getWeeklyTotals()
            val byCat     = db.getAmountByCategory()

            handler.post {
                tvTodayAmount.text = "₹%.2f".format(todayAmt)
                tvMonthAmount.text = "₹%.2f".format(monthAmt)
                tvTotalAmount.text = "₹%.2f".format(totalAmt)
                tvTodayCount.text  = "$todayCnt receipts today"
                tvMonthCount.text  = "$monthCnt total receipts"
                buildBarChart(weekly)
                buildCategoryBreakdown(byCat, totalAmt)
            }
        }
    }

    private fun buildBarChart(weekly: List<Pair<String, Double>>) {
        barChartContainer.removeAllViews()
        val max = weekly.maxOfOrNull { it.second } ?: 1.0
        val maxHeight = 120 // dp

        weekly.forEach { (day, amount) ->
            val col = LinearLayout(this)
            col.orientation = LinearLayout.VERTICAL
            col.gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
            val colParams = LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            colParams.setMargins(6, 0, 6, 0)
            col.layoutParams = colParams

            // Bar
            val barHeight = if (max > 0)
                ((amount / max) * maxHeight).toInt().coerceAtLeast(4)
            else 4
            val bar = View(this)
            val barParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (barHeight * resources.displayMetrics.density).toInt()
            )
            bar.layoutParams = barParams
            val barBg = android.graphics.drawable.GradientDrawable()
            barBg.cornerRadius = 8f * resources.displayMetrics.density
            barBg.setColor(
                if (amount > 0) Color.parseColor("#5C35A0")
                else Color.parseColor("#E8EAF0")
            )
            bar.background = barBg

            // Amount label above bar
            val tvAmt = TextView(this)
            tvAmt.text = if (amount > 0) "₹${amount.toInt()}" else ""
            tvAmt.textSize = 8f
            tvAmt.setTextColor(Color.parseColor("#5C35A0"))
            tvAmt.gravity = android.view.Gravity.CENTER
            val amtParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            tvAmt.layoutParams = amtParams

            // Day label
            val tvDay = TextView(this)
            tvDay.text = day
            tvDay.textSize = 10f
            tvDay.setTextColor(Color.parseColor("#6B7280"))
            tvDay.gravity = android.view.Gravity.CENTER
            val dayParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            tvDay.layoutParams = dayParams

            col.addView(tvAmt)
            col.addView(bar)
            col.addView(tvDay)
            barChartContainer.addView(col)
        }
    }

    private fun buildCategoryBreakdown(byCat: Map<String, Double>, total: Double) {
        categoryContainer.removeAllViews()
        if (byCat.isEmpty()) return

        byCat.entries.sortedByDescending { it.value }.forEach { (cat, amt) ->
            val row = LinearLayout(this)
            row.orientation = LinearLayout.VERTICAL
            val rowParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            rowParams.setMargins(0, 0, 0,
                (12 * resources.displayMetrics.density).toInt()
            )
            row.layoutParams = rowParams

            // Label row
            val labelRow = LinearLayout(this)
            labelRow.orientation = LinearLayout.HORIZONTAL
            labelRow.gravity = android.view.Gravity.CENTER_VERTICAL

            val emoji = CategoryHelper.getEmoji(cat)
            val tvLabel = TextView(this)
            tvLabel.text = "$emoji $cat"
            tvLabel.textSize = 13f
            tvLabel.setTextColor(CategoryHelper.getColorInt(cat))
            tvLabel.typeface = android.graphics.Typeface.DEFAULT_BOLD
            val labelParams = LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            tvLabel.layoutParams = labelParams

            val tvAmt = TextView(this)
            tvAmt.text = "₹%.2f".format(amt)
            tvAmt.textSize = 13f
            tvAmt.setTextColor(Color.parseColor("#1A1A2E"))
            tvAmt.typeface = android.graphics.Typeface.DEFAULT_BOLD

            labelRow.addView(tvLabel)
            labelRow.addView(tvAmt)

            // Progress bar
            val progress = FrameLayout(this)
            val progParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (8 * resources.displayMetrics.density).toInt()
            )
            progParams.topMargin = (6 * resources.displayMetrics.density).toInt()
            progress.layoutParams = progParams

            val track = View(this)
            val trackBg = android.graphics.drawable.GradientDrawable()
            trackBg.cornerRadius = 4f * resources.displayMetrics.density
            trackBg.setColor(Color.parseColor("#F0F0F5"))
            track.background = trackBg
            track.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )

            val fill = View(this)
            val fillBg = android.graphics.drawable.GradientDrawable()
            fillBg.cornerRadius = 4f * resources.displayMetrics.density
            fillBg.setColor(CategoryHelper.getColorInt(cat))
            fill.background = fillBg
            val pct = if (total > 0) (amt / total).toFloat() else 0f
            val fillParams = FrameLayout.LayoutParams(0,
                FrameLayout.LayoutParams.MATCH_PARENT)
            fill.layoutParams = fillParams
            fill.post {
                val w = progress.width
                (fill.layoutParams as FrameLayout.LayoutParams).width = (w * pct).toInt()
                fill.requestLayout()
            }

            progress.addView(track)
            progress.addView(fill)

            row.addView(labelRow)
            row.addView(progress)
            categoryContainer.addView(row)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}