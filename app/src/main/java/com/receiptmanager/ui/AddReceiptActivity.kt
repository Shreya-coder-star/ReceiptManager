package com.receiptmanager.ui

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.receiptmanager.R
import com.receiptmanager.db.DatabaseHelper
import com.receiptmanager.util.CategoryHelper
import com.receiptmanager.util.FileManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddReceiptActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var selectedDate: Calendar = Calendar.getInstance()
    private val dateFormat        = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("M/d/yyyy", Locale.getDefault())
    private var inputFileName  = ""
    private var createDetector = false
    private var cameraImageFile: File? = null
    private var selectedCategory = "Other"

    private lateinit var etTitle: EditText
    private lateinit var etDate: EditText
    private lateinit var etAmount: EditText
    private lateinit var tvFileStatus: TextView
    private lateinit var btnUploadFile: ImageButton
    private lateinit var btnCamera: ImageButton
    private lateinit var btnClearFile: ImageButton
    private lateinit var btnCreateReceipt: Button
    private lateinit var filePickerRow: View
    private lateinit var fileNameRow: View
    private lateinit var categoryChipGroup: LinearLayout

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val name = getFileNameFromUri(uri) ?: "receipt_${System.currentTimeMillis()}"
            inputFileName = FileManager.saveFilePermanently(this, uri, name)
            updateFileStatus()
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) cameraImageFile?.let {
            inputFileName = FileManager.saveImagePermanently(this, it)
            updateFileStatus()
        }
    }

    private val cameraPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera()
        else Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_receipt)

        db = DatabaseHelper(this)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "Add Receipt"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)

        etTitle           = findViewById(R.id.etTitle)
        etDate            = findViewById(R.id.etDate)
        etAmount          = findViewById(R.id.etAmount)
        tvFileStatus      = findViewById(R.id.tvFileStatus)
        btnUploadFile     = findViewById(R.id.btnUploadFile)
        btnCamera         = findViewById(R.id.btnCamera)
        btnClearFile      = findViewById(R.id.btnClearFile)
        btnCreateReceipt  = findViewById(R.id.btnCreateReceipt)
        filePickerRow     = findViewById(R.id.filePickerRow)
        fileNameRow       = findViewById(R.id.fileNameRow)
        categoryChipGroup = findViewById(R.id.categoryChipGroup)

        etDate.setText(displayDateFormat.format(selectedDate.time))
        etDate.isFocusable = false
        etDate.setOnClickListener { showDatePicker() }
        findViewById<ImageButton>(R.id.btnPickDate).setOnClickListener { showDatePicker() }

        buildCategoryChips()

        btnUploadFile.setOnClickListener { filePickerLauncher.launch("*/*") }
        btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) launchCamera()
            else cameraPermLauncher.launch(Manifest.permission.CAMERA)
        }
        btnClearFile.setOnClickListener {
            if (inputFileName.isNotEmpty()) {
                FileManager.deleteFile(this, inputFileName)
                inputFileName = ""
                updateFileStatus()
            }
        }
        btnCreateReceipt.setOnClickListener {
            val title  = etTitle.text.toString().trim()
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            if (title.isNotEmpty() && inputFileName.isNotEmpty()) {
                val result = db.insertReceipt(
                    title,
                    dateFormat.format(selectedDate.time),
                    inputFileName,
                    selectedCategory,
                    amount
                )
                if (result != -1L) { createDetector = true; finish() }
                else Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show()
            }
        }

        val watcher = object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) { updateCreateButton() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        etTitle.addTextChangedListener(watcher)

        updateFileStatus()
    }

    private fun buildCategoryChips() {
        categoryChipGroup.removeAllViews()
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
            updateChipStyle(chip, cat, cat == selectedCategory)
            chip.setOnClickListener {
                selectedCategory = cat
                for (i in 0 until categoryChipGroup.childCount) {
                    val c = categoryChipGroup.getChildAt(i) as TextView
                    updateChipStyle(c, CategoryHelper.CATEGORIES[i],
                        CategoryHelper.CATEGORIES[i] == selectedCategory)
                }
            }
            categoryChipGroup.addView(chip)
        }
    }

    private fun updateChipStyle(chip: TextView, category: String, selected: Boolean) {
        val color = CategoryHelper.getColorInt(category)
        val bg = android.graphics.drawable.GradientDrawable()
        bg.cornerRadius = 40f
        bg.setColor(
            if (selected) color
            else Color.parseColor("#22${CategoryHelper.getColor(category)}")
        )
        chip.background = bg
        chip.setTextColor(if (selected) Color.WHITE else color)
    }

    private fun showDatePicker() {
        DatePickerDialog(this,
            { _, year, month, day ->
                selectedDate.set(year, month, day)
                etDate.setText(displayDateFormat.format(selectedDate.time))
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateFileStatus() {
        if (inputFileName.isEmpty()) {
            filePickerRow.visibility = View.VISIBLE
            fileNameRow.visibility   = View.GONE
        } else {
            filePickerRow.visibility = View.GONE
            fileNameRow.visibility   = View.VISIBLE
            tvFileStatus.text = if (inputFileName.length > 35)
                inputFileName.substring(0, 35) else inputFileName
        }
        updateCreateButton()
    }

    private fun updateCreateButton() {
        btnCreateReceipt.visibility =
            if (inputFileName.isNotEmpty() && etTitle.text.isNotEmpty())
                View.VISIBLE else View.GONE
    }

    private fun launchCamera() {
        val f = File(cacheDir, "camera_${System.currentTimeMillis()}.jpg")
        cameraImageFile = f
        cameraLauncher.launch(
            FileProvider.getUriForFile(this, "${packageName}.fileprovider", f)
        )
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var name: String? = null
        contentResolver.query(uri, null, null, null, null)?.use {
            val idx = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && idx >= 0) name = it.getString(idx)
        }
        return name
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { handleBack(); return true }
        return super.onOptionsItemSelected(item)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { handleBack() }

    private fun handleBack() {
        if (!createDetector && inputFileName.isNotEmpty())
            FileManager.deleteFile(this, inputFileName)
        finish()
    }
}