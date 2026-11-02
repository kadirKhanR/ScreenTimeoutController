package com.kadir.screentimeout

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var statusText: TextView
    private lateinit var appModeButton: Button
    private var keepAwakeInApp = false

    private data class TimeoutOption(val label: String, val millis: Int)

    private val options = listOf(
        TimeoutOption("15 seconds", 15_000),
        TimeoutOption("30 seconds", 30_000),
        TimeoutOption("1 minute", 60_000),
        TimeoutOption("2 minutes", 120_000),
        TimeoutOption("5 minutes", 300_000),
        TimeoutOption("10 minutes", 600_000),
        TimeoutOption("30 minutes", 1_800_000),
        TimeoutOption("1 hour", 3_600_000),
        TimeoutOption("2 hours", 7_200_000),
        TimeoutOption("3 hours", 10_800_000),
        TimeoutOption("5 hours", 18_000_000),
        TimeoutOption("Never (maximum system value)", Int.MAX_VALUE)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildScreen()
        refreshStatus()
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    override fun onDestroy() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onDestroy()
    }

    private fun buildScreen() {
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(26), dp(22), dp(28))
            setBackgroundColor(0xFFF5F7FB.toInt())
        }
        scroll.addView(root)

        val title = text("Screen Timeout Controller", 25f, 0xFF14213D.toInt(), true)
        root.addView(title)
        root.addView(text("Control when your itel A90 screen turns off.", 14f, 0xFF667085.toInt(), false).withTop(7))

        val statusCard = card()
        statusText = text("Reading current timeout…", 16f, 0xFF14213D.toInt(), true)
        statusCard.addView(text("CURRENT SYSTEM TIMEOUT", 11f, 0xFF667085.toInt(), true))
        statusCard.addView(statusText.withTop(9))
        root.addView(statusCard.withTop(24))

        root.addView(sectionTitle("1. Whole phone" ).withTop(24))
        root.addView(text("Changes Android's system screen-off timer. It applies across other apps too, if itelOS allows the selected value.", 14f, 0xFF667085.toInt(), false).withTop(7))
        val globalButton = button("Choose system timeout") {
            if (Settings.System.canWrite(this)) showTimeoutPicker() else requestWriteSettings()
        }
        root.addView(globalButton.withTop(13))

        root.addView(sectionTitle("2. Only while this app is open").withTop(26))
        root.addView(text("Keeps the display awake while this app is visible. It turns off automatically when you leave or close this app.", 14f, 0xFF667085.toInt(), false).withTop(7))
        appModeButton = button("Turn ON — keep screen awake") { toggleAppMode() }
        root.addView(appModeButton.withTop(13))

        root.addView(button("Refresh current status") { refreshStatus() }.withTop(18))
        root.addView(button("Restore system timeout to 30 minutes") {
            applyTimeout(1_800_000)
        }.withTop(10))

        root.addView(text("Important", 15f, 0xFF14213D.toInt(), true).withTop(26))
        root.addView(text("• Android may restrict very long timeout values.\n• ‘Never’ uses the largest value Android's setting accepts; itelOS may still clamp it.\n• The app-only option works only while this screen is open.\n• Allow ‘Modify system settings’ when Android asks.", 13f, 0xFF667085.toInt(), false).withTop(7))

        setContentView(scroll)
    }

    private fun showTimeoutPicker() {
        val labels = options.map { it.label }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Select system timeout")
            .setItems(labels) { _, which -> applyTimeout(options[which].millis) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun applyTimeout(millis: Int) {
        if (!Settings.System.canWrite(this)) {
            requestWriteSettings()
            Toast.makeText(this, "Allow Modify system settings, then select the timeout again.", Toast.LENGTH_LONG).show()
            return
        }
        try {
            val success = Settings.System.putInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, millis)
            if (success) {
                Toast.makeText(this, "Timeout setting updated.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Android did not accept this timeout value.", Toast.LENGTH_LONG).show()
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Permission denied by system.", Toast.LENGTH_LONG).show()
        }
        refreshStatus()
    }

    private fun requestWriteSettings() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        } catch (e: Exception) {
            startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS))
        }
    }

    private fun toggleAppMode() {
        keepAwakeInApp = !keepAwakeInApp
        if (keepAwakeInApp) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            appModeButton.text = "Turn OFF — allow screen to sleep"
            Toast.makeText(this, "Screen will stay awake while this app is visible.", Toast.LENGTH_SHORT).show()
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            appModeButton.text = "Turn ON — keep screen awake"
            Toast.makeText(this, "App-only keep-awake is OFF.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshStatus() {
        if (!::statusText.isInitialized) return
        val current = try { Settings.System.getInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT) } catch (_: Exception) { -1 }
        statusText.text = if (current < 0) "Unavailable" else formatTimeout(current)
        if (::appModeButton.isInitialized) {
            appModeButton.text = if (keepAwakeInApp) "Turn OFF — allow screen to sleep" else "Turn ON — keep screen awake"
        }
    }

    private fun formatTimeout(ms: Int): String {
        if (ms == Int.MAX_VALUE) return "Maximum Android value (Never-like)"
        if (ms <= 0) return "System default / unavailable"
        val totalSeconds = ms / 1000
        return when {
            totalSeconds % 3600 == 0 && totalSeconds >= 3600 -> "${totalSeconds / 3600} hour(s)"
            totalSeconds % 60 == 0 && totalSeconds >= 60 -> "${totalSeconds / 60} minute(s)"
            else -> "$totalSeconds seconds"
        }
    }

    private fun sectionTitle(value: String) = text(value, 18f, 0xFF14213D.toInt(), true)

    private fun text(value: String, size: Float, color: Int, bold: Boolean): TextView = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
        setLineSpacing(dp(3).toFloat(), 1f)
    }

    private fun card(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(17), dp(17), dp(17), dp(17))
        setBackgroundColor(0xFFFFFFFF.toInt())
        elevation = dp(2).toFloat()
    }

    private fun button(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        textSize = 15f
        setOnClickListener { action() }
        minHeight = dp(50)
    }

    private fun View.withTop(value: Int): View {
        val params = (layoutParams as? LinearLayout.LayoutParams)
            ?: LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.topMargin = dp(value)
        layoutParams = params
        return this
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
