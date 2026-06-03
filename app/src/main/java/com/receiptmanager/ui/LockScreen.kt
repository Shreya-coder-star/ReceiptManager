package com.receiptmanager.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.receiptmanager.R
import com.receiptmanager.util.PinManager

class LockScreen : AppCompatActivity() {

    private lateinit var dot1: View
    private lateinit var dot2: View
    private lateinit var dot3: View
    private lateinit var dot4: View
    private lateinit var tvError: TextView
    private lateinit var btnFingerprint: ImageButton
    private var enteredPin = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock)

        dot1           = findViewById(R.id.dot1)
        dot2           = findViewById(R.id.dot2)
        dot3           = findViewById(R.id.dot3)
        dot4           = findViewById(R.id.dot4)
        tvError        = findViewById(R.id.tvPinError)
        btnFingerprint = findViewById(R.id.btnFingerprint)

        setupKeypad()
        updateDots()

        if (PinManager.isFingerprintEnabled(this) && isBiometricAvailable()) {
            btnFingerprint.visibility = View.VISIBLE
            showBiometricPrompt()
        } else {
            btnFingerprint.visibility = View.GONE
        }

        btnFingerprint.setOnClickListener {
            showBiometricPrompt()
        }
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
        tvError.visibility = View.GONE

        if (enteredPin.length == 4) {
            if (PinManager.verifyPin(this, enteredPin)) {
                unlockApp()
            } else {
                tvError.text = "Wrong PIN. Try again."
                tvError.visibility = View.VISIBLE
                enteredPin = ""
                updateDots()
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

    private fun unlockApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("unlocked", true)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }

    private fun isBiometricAvailable(): Boolean {
        val bm = BiometricManager.from(this)
        return bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
                BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(
                result: BiometricPrompt.AuthenticationResult
            ) {
                unlockApp()
            }
            override fun onAuthenticationError(
                errorCode: Int, errString: CharSequence
            ) {
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                    errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                ) {
                    tvError.text = errString.toString()
                    tvError.visibility = View.VISIBLE
                }
            }
            override fun onAuthenticationFailed() {
                tvError.text = "Fingerprint not recognized. Try again."
                tvError.visibility = View.VISIBLE
            }
        }

        val prompt = BiometricPrompt(this, executor, callback)
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Receipt Manager")
            .setSubtitle("Use your fingerprint to unlock")
            .setNegativeButtonText("Use PIN instead")
            .build()
        prompt.authenticate(info)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finishAffinity()
    }
}