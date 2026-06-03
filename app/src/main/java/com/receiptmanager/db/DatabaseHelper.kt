package com.receiptmanager.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.receiptmanager.model.Receipt

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "recep.db"
        private const val DATABASE_VERSION = 3  // bumped to 3

        const val TABLE_RECEIPT = "receipt_bill"
        const val COL_ID = "ID"
        const val COL_TITLE = "receipt_title"
        const val COL_DATE = "recep_bill_date"
        const val COL_PATH = "recep_bill_path"
        const val COL_CATEGORY = "receipt_category"
        const val COL_AMOUNT = "receipt_amount"  // NEW
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_RECEIPT (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                $COL_TITLE TEXT,
                $COL_DATE TEXT,
                $COL_PATH TEXT,
                $COL_CATEGORY TEXT DEFAULT 'Other',
                $COL_AMOUNT REAL DEFAULT 0.0
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_RECEIPT ADD COLUMN $COL_CATEGORY TEXT DEFAULT 'Other'")
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE $TABLE_RECEIPT ADD COLUMN $COL_AMOUNT REAL DEFAULT 0.0")
        }
    }

    fun insertReceipt(
        title: String,
        date: String,
        path: String,
        category: String = "Other",
        amount: Double = 0.0
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TITLE, title)
            put(COL_DATE, date)
            put(COL_PATH, path)
            put(COL_CATEGORY, category)
            put(COL_AMOUNT, amount)
        }
        val result = db.insert(TABLE_RECEIPT, null, values)
        db.close()
        return result
    }

    fun getAllReceipts(): List<Receipt> {
        val list = mutableListOf<Receipt>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_RECEIPT ORDER BY $COL_TITLE ASC", null
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    Receipt(
                        id       = it.getInt(it.getColumnIndexOrThrow(COL_ID)),
                        title    = it.getString(it.getColumnIndexOrThrow(COL_TITLE)),
                        date     = it.getString(it.getColumnIndexOrThrow(COL_DATE)),
                        filePath = it.getString(it.getColumnIndexOrThrow(COL_PATH)),
                        category = it.getString(it.getColumnIndexOrThrow(COL_CATEGORY)) ?: "Other",
                        amount   = it.getDouble(it.getColumnIndexOrThrow(COL_AMOUNT))
                    )
                )
            }
        }
        db.close()
        return list
    }

    fun getReceiptsByDate(date: String): List<Receipt> {
        val list = mutableListOf<Receipt>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_RECEIPT WHERE $COL_DATE = ? ORDER BY $COL_TITLE ASC",
            arrayOf(date)
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    Receipt(
                        id       = it.getInt(it.getColumnIndexOrThrow(COL_ID)),
                        title    = it.getString(it.getColumnIndexOrThrow(COL_TITLE)),
                        date     = it.getString(it.getColumnIndexOrThrow(COL_DATE)),
                        filePath = it.getString(it.getColumnIndexOrThrow(COL_PATH)),
                        category = it.getString(it.getColumnIndexOrThrow(COL_CATEGORY)) ?: "Other",
                        amount   = it.getDouble(it.getColumnIndexOrThrow(COL_AMOUNT))
                    )
                )
            }
        }
        db.close()
        return list
    }

    fun getReceiptsByCategory(category: String): List<Receipt> {
        val list = mutableListOf<Receipt>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_RECEIPT WHERE $COL_CATEGORY = ? ORDER BY $COL_TITLE ASC",
            arrayOf(category)
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    Receipt(
                        id       = it.getInt(it.getColumnIndexOrThrow(COL_ID)),
                        title    = it.getString(it.getColumnIndexOrThrow(COL_TITLE)),
                        date     = it.getString(it.getColumnIndexOrThrow(COL_DATE)),
                        filePath = it.getString(it.getColumnIndexOrThrow(COL_PATH)),
                        category = it.getString(it.getColumnIndexOrThrow(COL_CATEGORY)) ?: "Other",
                        amount   = it.getDouble(it.getColumnIndexOrThrow(COL_AMOUNT))
                    )
                )
            }
        }
        db.close()
        return list
    }

    fun searchReceiptsByTitle(title: String): List<Receipt> {
        val list = mutableListOf<Receipt>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_RECEIPT WHERE $COL_TITLE LIKE ?",
            arrayOf("%$title%")
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    Receipt(
                        id       = it.getInt(it.getColumnIndexOrThrow(COL_ID)),
                        title    = it.getString(it.getColumnIndexOrThrow(COL_TITLE)),
                        date     = it.getString(it.getColumnIndexOrThrow(COL_DATE)),
                        filePath = it.getString(it.getColumnIndexOrThrow(COL_PATH)),
                        category = it.getString(it.getColumnIndexOrThrow(COL_CATEGORY)) ?: "Other",
                        amount   = it.getDouble(it.getColumnIndexOrThrow(COL_AMOUNT))
                    )
                )
            }
        }
        db.close()
        return list
    }

    fun getCategorySummary(): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_CATEGORY, COUNT(*) as count FROM $TABLE_RECEIPT GROUP BY $COL_CATEGORY",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                map[it.getString(0) ?: "Other"] = it.getInt(1)
            }
        }
        db.close()
        return map
    }

    // Total spent today
    fun getTotalAmountByDate(date: String): Double {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COL_AMOUNT) FROM $TABLE_RECEIPT WHERE $COL_DATE = ?",
            arrayOf(date)
        )
        var total = 0.0
        cursor.use { if (it.moveToFirst()) total = it.getDouble(0) }
        db.close()
        return total
    }

    // Total spent this month
    fun getTotalAmountByMonth(month: String): Double {
        val db = readableDatabase
        // month format: MM-yyyy
        val cursor = db.rawQuery(
            "SELECT SUM($COL_AMOUNT) FROM $TABLE_RECEIPT WHERE $COL_DATE LIKE ?",
            arrayOf("%-$month")
        )
        var total = 0.0
        cursor.use { if (it.moveToFirst()) total = it.getDouble(0) }
        db.close()
        return total
    }

    // Total overall
    fun getTotalAmountAll(): Double {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COL_AMOUNT) FROM $TABLE_RECEIPT", null
        )
        var total = 0.0
        cursor.use { if (it.moveToFirst()) total = it.getDouble(0) }
        db.close()
        return total
    }

    // Amount by category
    fun getAmountByCategory(): Map<String, Double> {
        val map = mutableMapOf<String, Double>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_CATEGORY, SUM($COL_AMOUNT) FROM $TABLE_RECEIPT GROUP BY $COL_CATEGORY",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                map[it.getString(0) ?: "Other"] = it.getDouble(1)
            }
        }
        db.close()
        return map
    }

    // Weekly totals for last 7 days
    fun getWeeklyTotals(): List<Pair<String, Double>> {
        val result = mutableListOf<Pair<String, Double>>()
        val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
        val daySdf = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
        val cal = java.util.Calendar.getInstance()
        val db = readableDatabase
        for (i in 6 downTo 0) {
            cal.time = java.util.Date()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -i)
            val dateStr = sdf.format(cal.time)
            val dayLabel = daySdf.format(cal.time)
            val cursor = db.rawQuery(
                "SELECT SUM($COL_AMOUNT) FROM $TABLE_RECEIPT WHERE $COL_DATE = ?",
                arrayOf(dateStr)
            )
            var total = 0.0
            cursor.use { if (it.moveToFirst()) total = it.getDouble(0) }
            result.add(Pair(dayLabel, total))
        }
        db.close()
        return result
    }

    fun deleteReceipt(id: Int): Int {
        val db = writableDatabase
        val result = db.delete(TABLE_RECEIPT, "$COL_ID = ?", arrayOf(id.toString()))
        db.close()
        return result
    }

    fun getTotalCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_RECEIPT", null)
        var count = 0
        cursor.use { if (it.moveToFirst()) count = it.getInt(0) }
        db.close()
        return count
    }

    fun getCountByDate(date: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_RECEIPT WHERE $COL_DATE = ?", arrayOf(date)
        )
        var count = 0
        cursor.use { if (it.moveToFirst()) count = it.getInt(0) }
        db.close()
        return count
    }
}