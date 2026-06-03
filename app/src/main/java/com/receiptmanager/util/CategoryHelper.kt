package com.receiptmanager.util

object CategoryHelper {

    val CATEGORIES = listOf(
        "Groceries",
        "Food & Dining",
        "Medical",
        "Travel",
        "Shopping",
        "Utilities",
        "Entertainment",
        "Education",
        "Fuel",
        "Other"
    )

    fun getEmoji(category: String): String {
        return when (category) {
            "Groceries"       -> "🛒"
            "Food & Dining"   -> "🍕"
            "Medical"         -> "🏥"
            "Travel"          -> "✈️"
            "Shopping"        -> "🛍️"
            "Utilities"       -> "💡"
            "Entertainment"   -> "🎬"
            "Education"       -> "📚"
            "Fuel"            -> "⛽"
            else              -> "📂"
        }
    }

    fun getColor(category: String): String {
        return when (category) {
            "Groceries"       -> "43A047"
            "Food & Dining"   -> "FB8C00"
            "Medical"         -> "E53935"
            "Travel"          -> "1E88E5"
            "Shopping"        -> "8E24AA"
            "Utilities"       -> "00ACC1"
            "Entertainment"   -> "F06292"
            "Education"       -> "5C35A0"
            "Fuel"            -> "6D4C41"
            else              -> "6B7280"
        }
    }

    fun getColorInt(category: String): Int {
        return android.graphics.Color.parseColor("#${getColor(category)}")
    }
}