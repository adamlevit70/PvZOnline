package com.example.pvzonline

import android.Manifest
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class NavigationActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    companion object {
        private const val PREFS = "game_prefs"
        private const val KEY_ALARM_PERMISSION_ASKED = "alarm_permission_asked"
        private const val KEY_REMINDER_ENABLED = "reminder_enabled"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        enableEdgeToEdge()
        setContentView(R.layout.activity_navigation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply padding for top, left, and right system bars, but set bottom padding to 0
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        bottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.nav_user_details -> {
                    replaceFragment(UserDetailsFragment())
                    true
                }
                else -> false
            }
        }

        // Load the home fragment by default
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.navigationFragmentContainer, fragment)
            .commit()
    }


    override fun onStart() {
        super.onStart()
        showAlarmPermissionDialogIfNeeded()
        cancelGardenReminder() // cancel old alarm to avoid duplicates
    }

    override fun onStop() {
        super.onStop()
        scheduleGardenReminderIfAllowed()
    }

    fun switchToJoinRoomFragment() {
        replaceFragment(JoinRoomFragment())
    }

    fun goToWaitingRoom(code: String) {
        val intent = Intent(this, WaitingRoomActivity::class.java)
        intent.putExtra("ROOM_CODE", code)
        startActivity(intent)
    }


    // Show dialog only once
    private fun showAlarmPermissionDialogIfNeeded() {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        if (prefs.getBoolean(KEY_ALARM_PERMISSION_ASKED, false)) return

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
            prefs.edit().putBoolean(KEY_REMINDER_ENABLED, true).apply()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Enable Garden Reminder 🌱")
            .setMessage("We'll remind you if you haven't visited your garden for a day.")
            .setPositiveButton("Allow") { _, _ ->
                prefs.edit().putBoolean(KEY_ALARM_PERMISSION_ASKED, true)
                    .putBoolean(KEY_REMINDER_ENABLED, true).apply()
                startActivity(Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            }
            .setNegativeButton("No thanks") { _, _ ->
                prefs.edit().putBoolean(KEY_ALARM_PERMISSION_ASKED, true)
                    .putBoolean(KEY_REMINDER_ENABLED, false).apply()
            }
            .setCancelable(false)
            .show()
    }

    // Schedule reminder if user allowed
    private fun scheduleGardenReminderIfAllowed() {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_REMINDER_ENABLED, false)) return

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Only schedule if not already scheduled
        if (PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE) != null) return

        //val triggerTime = System.currentTimeMillis() + 24 * 60 * 60 * 1000L // 1 day
        val triggerTime = System.currentTimeMillis() + 1000L * 10 // 10 sec

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
    }

    // Cancel existing alarm
    private fun cancelGardenReminder() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}