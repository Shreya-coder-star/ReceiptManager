package com.receiptmanager.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.receiptmanager.R
import com.receiptmanager.util.PinManager

class SetupPinActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var tvError: TextView
    private lateinit var dot1: View
    private lateinit var dot2: View
    private lateinit var dot3: View
    private lateinit var dot4: View
    private var enteredPin = ""
    private var firstPin   = ""
    private var isConfirm  = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_pin)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        supportActionBar?.title = "Set PIN"

        tvTitle    = findViewById(R.id.tvPinTitle)
        tvSubtitle = findViewById(R.id.tvPinSubtitle)
        tvError    = findViewById(R.id.tvPinError)
        dot1       = findViewById(R.id.dot1)
        dot2       = findViewById(R.id.dot2)
        dot3       = findViewById(R.id.dot3)
        dot4       = findViewById(R.id.dot4)

        tvTitle.text    = "Create PIN"
        tvSubtitle.text = "Enter a 4-digit PIN to secure your app"

        updateDots()
        setupKeypad()
    }

    private fun setupKeypad() {
        for (i in 0..9) {
            val id = resources.getIdentifier("btn$i", "id", packageName)
            findViewById<Button>(id).setOnClickListener {
                onKeyPress(i.toString())
            }
        }
        findViewById<ImageButton>(R.id.btnBackspace).setOnClickListener {
            if (enteredPin.isNotEmpty()) {
                enteredPin = enteredPin.dropLast(1)
                updateDots()
            }
        }
    }

    private fun onKeyPress(digit: String) {
        if (enteredPin.length >= 4) return
        enteredPin += digit
        updateDots()

        if (enteredPin.length == 4) {
            if (!isConfirm) {
                firstPin   = enteredPin
                enteredPin = ""
                isConfirm  = true
                tvTitle.text    = "Confirm PIN"
                tvSubtitle.text = "Enter your PIN again to confirm"
                tvError.visibility = View.GONE
                updateDots()
            } else {
                if (enteredPin == firstPin) {
                    PinManager.savePin(this, enteredPin)
                    PinManager.setPinEnabled(this, true)
                    Toast.makeText(this, "✅ PIN set successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    tvError.text = "PINs don't match. Try again."
                    tvError.visibility = View.VISIBLE
                    enteredPin = ""
                    firstPin   = ""
                    isConfirm  = false
                    tvTitle.text    = "Create PIN"
                    tvSubtitle.text = "Enter a 4-digit PIN to secure your app"
                    updateDots()
                }
            }
        }
    }

    private fun updateDots() {
        val dots = listOf(dot1, dot2, dot3, dot4)
        dots.forEachIndexed { index, dot ->
            if (index < enteredPin.length) {
                dot.setBackgroundResource(R.drawable.pin_dot_filled)
            } else {
                dot.setBackgroundResource(R.drawable.pin_dot_empty)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}