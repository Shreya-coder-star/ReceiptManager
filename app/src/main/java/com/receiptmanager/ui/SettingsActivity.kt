package com.receiptmanager.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import com.receiptmanager.R
import com.receiptmanager.util.PinManager
import com.receiptmanager.util.ThemeManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var switchPin: Switch
    private lateinit var switchFingerprint: Switch
    private lateinit var switchDarkMode: Switch
    private lateinit var btnChangePin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "Settings"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)

        switchPin         = findViewById(R.id.switchPin)
        switchFingerprint = findViewById(R.id.switchFingerprint)
        switchDarkMode    = findViewById(R.id.switchDarkMode)
        btnChangePin      = findViewById(R.id.btnChangePin)

        // Load saved states
        switchPin.isChecked         = PinManager.isPinEnabled(this)
        switchFingerprint.isChecked = PinManager.isFingerprintEnabled(this)
        switchDarkMode.isChecked    = ThemeManager.isDarkMode(this)

        updateFingerprintAvailability()

        switchPin.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                // Only go to setup if no PIN exists yet
                if (PinManager.getPin(this) == null) {
                    startActivity(Intent(this, SetupPinActivity::class.java))
                } else {
                    PinManager.setPinEnabled(this, true)
                }
            } else {
                PinManager.setPinEnabled(this, false)
                PinManager.setFingerprintEnabled(this, false)
                switchFingerprint.isChecked = false
                Toast.makeText(this, "PIN disabled", Toast.LENGTH_SHORT).show()
            }
        }

        switchFingerprint.setOnCheckedChangeListener { _, checked ->
            if (checked && !PinManager.isPinEnabled(this)) {
                switchFingerprint.isChecked = false
                Toast.makeText(this, "Set a PIN first", Toast.LENGTH_SHORT).show()
                return@setOnCheckedChangeListener
            }
            PinManager.setFingerprintEnabled(this, checked)
        }

        switchDarkMode.setOnCheckedChangeListener { _, checked ->
            ThemeManager.setDarkMode(this, checked)
            recreate()
        }

        btnChangePin.setOnClickListener {
            startActivity(Intent(this, SetupPinActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        switchPin.isChecked = PinManager.isPinEnabled(this)
    }

    private fun updateFingerprintAvailability() {
        val bm = BiometricManager.from(this)
        val available = bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
                BiometricManager.BIOMETRIC_SUCCESS
        switchFingerprint.isEnabled = available
        if (!available) {
            switchFingerprint.isChecked = false
            findViewById<TextView>(R.id.tvFingerprintHint).text =
                "Fingerprint not available on this device"
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}