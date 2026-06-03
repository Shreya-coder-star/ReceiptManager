package com.receiptmanager.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.receiptmanager.R
import com.receiptmanager.adapter.ReceiptAdapter
import com.receiptmanager.db.DatabaseHelper
import com.receiptmanager.model.Receipt
import com.receiptmanager.util.FileManager
import com.receiptmanager.util.PinManager
import com.receiptmanager.util.ThemeManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReceiptAdapter
    private lateinit var recyclerSearchView: RecyclerView
    private lateinit var adapterSearch: ReceiptAdapter
    private lateinit var layoutReceiptTab: View
    private lateinit var layoutSearchTab: View
    private lateinit var tvCurrentDate: TextView
    private lateinit var btnAddReceipt: View
    private lateinit var btnExpense: View
    private lateinit var btnSettings: View
    private lateinit var tvCalendarToggle: TextView
    private lateinit var ivCalendarChevron: ImageView
    private lateinit var toolbarSearch: Toolbar

    private var currentIndex = 0
    private var selectedDate: Calendar = Calendar.getInstance()
    private var isUnlocked = false

    private val dateFormat        = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("EEE, MMM d yyyy", Locale.getDefault())
    private val handler           = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply saved theme
        ThemeManager.applySavedTheme(this)

        setContentView(R.layout.activity_main)

        db = DatabaseHelper(this)

        recyclerView       = findViewById(R.id.recyclerViewReceipts)
        recyclerSearchView = findViewById(R.id.recyclerViewSearch)
        layoutReceiptTab   = findViewById(R.id.layoutReceiptTab)
        layoutSearchTab    = findViewById(R.id.layoutSearchTab)
        tvCurrentDate      = findViewById(R.id.tvCurrentDate)
        btnAddReceipt      = findViewById(R.id.btnAddReceipt)
        btnExpense         = findViewById(R.id.btnExpense)
        btnSettings        = findViewById(R.id.btnSettings)
        tvCalendarToggle   = findViewById(R.id.tvCalendarToggle)
        ivCalendarChevron  = findViewById(R.id.ivCalendarChevron)
        toolbarSearch      = findViewById(R.id.toolbarSearch)

        setSupportActionBar(toolbarSearch)
        supportActionBar?.title = "Search Receipts"

        tvCurrentDate.text = displayDateFormat.format(selectedDate.time)

        adapter = ReceiptAdapter(this, mutableListOf(),
            onOpen   = { openFile(it) },
            onShare  = { shareFile(it) },
            onDelete = { confirmDelete(it) })
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)

        adapterSearch = ReceiptAdapter(this, mutableListOf(),
            onOpen   = { openFile(it) },
            onShare  = { shareFile(it) },
            onDelete = { confirmDelete(it) })
        recyclerSearchView.layoutManager = LinearLayoutManager(this)
        recyclerSearchView.adapter = adapterSearch
        recyclerSearchView.setHasFixedSize(true)

        // Read unlocked flag from LockScreen
        if (intent.getBooleanExtra("unlocked", false)) {
            isUnlocked = true
        }

        btnAddReceipt.setOnClickListener {
            startActivity(Intent(this, AddReceiptActivity::class.java))
        }

        btnExpense.setOnClickListener {
            startActivity(Intent(this, ExpenseActivity::class.java))
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        val calendarClick = View.OnClickListener {
            DatePickerDialog(this,
                { _, year, month, dayOfMonth ->
                    selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, dayOfMonth)
                    tvCurrentDate.text    = displayDateFormat.format(selectedDate.time)
                    tvCalendarToggle.text = displayDateFormat.format(selectedDate.time)
                    loadReceiptsByDateAsync(dateFormat.format(selectedDate.time))
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        tvCalendarToggle.setOnClickListener(calendarClick)
        ivCalendarChevron.setOnClickListener(calendarClick)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_receipt -> {
                    currentIndex = 0
                    showReceiptTab()
                    selectedDate          = Calendar.getInstance()
                    tvCurrentDate.text    = displayDateFormat.format(selectedDate.time)
                    tvCalendarToggle.text = "Select Date"
                    loadReceiptsByDateAsync(dateFormat.format(selectedDate.time))
                    invalidateOptionsMenu()
                    true
                }
                R.id.nav_search -> {
                    currentIndex = 1
                    showSearchTab()
                    loadAllReceiptsAsync()
                    invalidateOptionsMenu()
                    true
                }
                R.id.nav_category -> {
                    startActivity(Intent(this, CategoryActivity::class.java))
                    true
                }
                else -> false
            }
        }

        loadReceiptsByDateAsync(dateFormat.format(selectedDate.time))
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.getBooleanExtra("unlocked", false) == true) {
            isUnlocked = true
        }
    }

    override fun onResume() {
        super.onResume()

        // Check PIN every time app comes to foreground
        if (PinManager.isPinEnabled(this) &&
            PinManager.getPin(this) != null &&
            !isUnlocked) {
            isUnlocked = false
            startActivity(Intent(this, LockScreen::class.java))
            return
        }

        // Reset unlock flag after successful load
        isUnlocked = false

        if (currentIndex == 0) loadReceiptsByDateAsync(dateFormat.format(selectedDate.time))
        else loadAllReceiptsAsync()
    }

    private fun loadReceiptsByDateAsync(date: String) {
        thread {
            val data = db.getReceiptsByDate(date)
            handler.post { adapter.updateData(data) }
        }
    }

    private fun loadAllReceiptsAsync() {
        thread {
            val data = db.getAllReceipts()
            handler.post { adapterSearch.updateData(data) }
        }
    }

    private fun showReceiptTab() {
        layoutReceiptTab.visibility = View.VISIBLE
        layoutSearchTab.visibility  = View.GONE
    }

    private fun showSearchTab() {
        layoutReceiptTab.visibility = View.GONE
        layoutSearchTab.visibility  = View.VISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (currentIndex == 1) {
            menuInflater.inflate(R.menu.menu_search, menu)
            val searchItem = menu.findItem(R.id.action_search)
            val searchView = searchItem?.actionView as? SearchView
            searchView?.queryHint = "Search receipts..."
            val et = searchView?.findViewById<EditText>(
                androidx.appcompat.R.id.search_src_text
            )
            et?.setTextColor(android.graphics.Color.WHITE)
            et?.setHintTextColor(android.graphics.Color.LTGRAY)
            searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?) = false
                override fun onQueryTextChange(newText: String?): Boolean {
                    thread {
                        val results = if (!newText.isNullOrEmpty())
                            db.searchReceiptsByTitle(newText)
                        else db.getAllReceipts()
                        handler.post { adapterSearch.updateData(results) }
                    }
                    return true
                }
            })
        }
        return true
    }

    private fun openFile(receipt: Receipt) {
        val file = File(FileManager.returnFilePath(this, receipt.filePath))
        if (!file.exists()) {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
            return
        }
        val uri    = FileManager.getShareableUri(this, receipt.filePath)
        val mime   = FileManager.getMimeType(receipt.filePath) ?: "*/*"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try { startActivity(intent) }
        catch (e: Exception) {
            Toast.makeText(this, "No app found to open this file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareFile(receipt: Receipt) {
        val file = File(FileManager.returnFilePath(this, receipt.filePath))
        if (!file.exists()) {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
            return
        }
        val uri         = FileManager.getShareableUri(this, receipt.filePath)
        val mime        = FileManager.getMimeType(receipt.filePath) ?: "*/*"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, receipt.title)
            putExtra(Intent.EXTRA_TEXT, receipt.title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, receipt.title))
    }

    private fun confirmDelete(receipt: Receipt) {
        AlertDialog.Builder(this)
            .setTitle("Delete Receipt")
            .setMessage("Permanently delete \"${receipt.title}\"?")
            .setPositiveButton("DELETE") { _, _ ->
                thread {
                    FileManager.deleteFile(this, receipt.filePath)
                    db.deleteReceipt(receipt.id)
                    val data = if (currentIndex == 0)
                        db.getReceiptsByDate(dateFormat.format(selectedDate.time))
                    else db.getAllReceipts()
                    handler.post {
                        if (currentIndex == 0) adapter.updateData(data)
                        else adapterSearch.updateData(data)
                    }
                }
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }
}