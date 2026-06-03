package com.receiptmanager.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.receiptmanager.R
import com.receiptmanager.adapter.ReceiptAdapter
import com.receiptmanager.db.DatabaseHelper
import com.receiptmanager.model.Receipt
import com.receiptmanager.util.CategoryHelper
import com.receiptmanager.util.FileManager
import java.io.File
import kotlin.concurrent.thread
import android.os.Handler
import android.os.Looper
import android.graphics.Color
import androidx.appcompat.app.AlertDialog

class CategoryActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReceiptAdapter
    private lateinit var tvCategoryTitle: TextView
    private lateinit var chipGroup: LinearLayout
    private var selectedCategory = CategoryHelper.CATEGORIES[0]
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        db = DatabaseHelper(this)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "Categories"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)

        tvCategoryTitle = findViewById(R.id.tvCategoryTitle)
        chipGroup       = findViewById(R.id.categoryFilterChips)
        recyclerView    = findViewById(R.id.recyclerViewCategory)

        adapter = ReceiptAdapter(this, mutableListOf(),
            onOpen   = { openFile(it) },
            onShare  = { shareFile(it) },
            onDelete = { confirmDelete(it) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)

        buildChips()
        loadCategory(selectedCategory)
    }

    private fun buildChips() {
        chipGroup.removeAllViews()
        CategoryHelper.CATEGORIES.forEach { cat ->
            val chip = TextView(this)
            chip.text = "${CategoryHelper.getEmoji(cat)} $cat"
            chip.textSize = 12f
            chip.setPadding(24, 12, 24, 12)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 12, 0)
            chip.layoutParams = params
            styleChip(chip, cat, cat == selectedCategory)
            chip.setOnClickListener {
                selectedCategory = cat
                for (i in 0 until chipGroup.childCount) {
                    val c = chipGroup.getChildAt(i) as TextView
                    styleChip(c, CategoryHelper.CATEGORIES[i], CategoryHelper.CATEGORIES[i] == selectedCategory)
                }
                loadCategory(selectedCategory)
            }
            chipGroup.addView(chip)
        }
    }

    private fun styleChip(chip: TextView, category: String, selected: Boolean) {
        val color = CategoryHelper.getColorInt(category)
        val bg = android.graphics.drawable.GradientDrawable()
        bg.cornerRadius = 40f
        bg.setColor(if (selected) color else Color.parseColor("#22${CategoryHelper.getColor(category)}"))
        chip.background = bg
        chip.setTextColor(if (selected) Color.WHITE else color)
    }

    private fun loadCategory(category: String) {
        val emoji = CategoryHelper.getEmoji(category)
        tvCategoryTitle.text = "$emoji $category"
        thread {
            val data = db.getReceiptsByCategory(category)
            handler.post { adapter.updateData(data) }
        }
    }

    private fun openFile(receipt: Receipt) {
        val file = File(FileManager.returnFilePath(this, receipt.filePath))
        if (!file.exists()) { Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show(); return }
        val uri    = FileManager.getShareableUri(this, receipt.filePath)
        val mime   = FileManager.getMimeType(receipt.filePath) ?: "*/*"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try { startActivity(intent) }
        catch (e: Exception) { Toast.makeText(this, "No app to open file", Toast.LENGTH_SHORT).show() }
    }

    private fun shareFile(receipt: Receipt) {
        val uri  = FileManager.getShareableUri(this, receipt.filePath)
        val mime = FileManager.getMimeType(receipt.filePath) ?: "*/*"
        val i    = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(i, receipt.title))
    }

    private fun confirmDelete(receipt: Receipt) {
        AlertDialog.Builder(this)
            .setTitle("Delete Receipt")
            .setMessage("Permanently delete \"${receipt.title}\"?")
            .setPositiveButton("DELETE") { _, _ ->
                thread {
                    FileManager.deleteFile(this, receipt.filePath)
                    db.deleteReceipt(receipt.id)
                    val data = db.getReceiptsByCategory(selectedCategory)
                    handler.post { adapter.updateData(data) }
                }
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}