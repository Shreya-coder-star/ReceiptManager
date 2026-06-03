package com.receiptmanager.model

data class Receipt(
    val id: Int = 0,
    val title: String,
    val date: String,
    val filePath: String,
    val category: String = "Other",
    val amount: Double = 0.0
)