package com.receiptmanager.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.receiptmanager.R
import com.receiptmanager.util.PinManager
import com.receiptmanager.util.ThemeManager

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply saved theme first
        ThemeManager.applySavedTheme(this)

        setContentView(R.layout.activity_splash)

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )

        val logo    = findViewById<ImageView>(R.id.splashLogo)
        val appName = findViewById<TextView>(R.id.splashAppName)
        val tagline = findViewById<TextView>(R.id.splashTagline)

        logo.alpha = 0f
        logo.animate().alpha(1f).scaleX(1.1f).scaleY(1.1f).setDuration(600)
            .withEndAction {
                logo.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
            }.start()

        appName.alpha = 0f
        tagline.alpha = 0f
        Handler(Looper.getMainLooper()).postDelayed({
            appName.animate().alpha(1f).setDuration(400).start()
            tagline.animate().alpha(1f).setDuration(500).start()
        }, 400)

        Handler(Looper.getMainLooper()).postDelayed({
            navigateNext()
        }, 2500)
    }

    private fun navigateNext() {
        val pinEnabled         = PinManager.isPinEnabled(this)
        val pinExists          = PinManager.getPin(this) != null

        if (pinEnabled && pinExists) {
            // Go to lock screen
            startActivity(Intent(this, LockScreen::class.java))
        } else {
            // Go directly to main
            startActivity(Intent(this, MainActivity::class.java))
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}