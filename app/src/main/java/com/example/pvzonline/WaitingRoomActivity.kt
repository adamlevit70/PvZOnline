package com.example.pvzonline

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WaitingRoomActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val roomsRef = db.collection("rooms")

    private var hasGuest = false
    private var canStartGame = false
    private var gameStartedLocally = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        enableEdgeToEdge()
        setContentView(R.layout.activity_waiting_room)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val roomCode = intent.getStringExtra("ROOM_CODE")
        val roomCodeText = findViewById<TextView>(R.id.roomCodeText)
        val btnStartGame = findViewById<Button>(R.id.btnStartGame)

        roomCodeText.text = "ROOM CODE: " + roomCode

        btnStartGame.setOnClickListener {
            if(canStartGame) {
                if(roomCode != null) {
                    roomsRef.document(roomCode)
                        .update("gameStarted", true)
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to start game", Toast.LENGTH_LONG).show()
                        }
                }
                else {
                    Toast.makeText(this, "Room code is null", Toast.LENGTH_LONG).show()
                }
            }
        }

        if(roomCode != null) {
            listenToRoom(roomCode)
        }
    }

    fun listenToRoom(roomCode: String) {
        roomsRef.document(roomCode)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val hostId = snapshot.getString("hostId")
                    val guestId = snapshot.getString("guestId")
                    val gameStarted = snapshot.getBoolean("gameStarted")

                    updatePlayerTexts(hostId, guestId)

                    if (!hasGuest && guestId != null) {
                        hasGuest = true
                        onGuestJoined(hostId)
                    }

                    if (gameStarted == true && !gameStartedLocally) {
                        gameStartedLocally = true
                        startGame()
                    }
                }
            }
    }

    fun onGuestJoined(hostId: String?) {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser!!.uid

        if(hostId == userId) {
            canStartGame = true
            val btnStartGame = findViewById<Button>(R.id.btnStartGame)
            btnStartGame.visibility = View.VISIBLE
        }
    }


    fun startGame() {
        val roomCode = intent.getStringExtra("ROOM_CODE") ?: return

        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("ROOM_CODE", roomCode)
        startActivity(intent)
        finish()
    }


    fun updatePlayerTexts(hostId: String?, guestId: String?) {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser!!.uid

        val textHostPlayer = findViewById<TextView>(R.id.textHostPlayer)
        val textGuestPlayer = findViewById<TextView>(R.id.textGuestPlayer)

        if (hostId == userId) {
            // Host = You
            getUserLevel(userId) { level ->
                textHostPlayer.text = "Host: You (Lv. ${level ?: "?"})"
            }

            if (guestId == null) {
                textGuestPlayer.text = "Guest: Waiting for player..."
            } else {
                getUserLevel(guestId) { level ->
                    textGuestPlayer.text = "Guest: Connected (Lv. ${level ?: "?"})"
                }
            }

        } else {
            // Guest = You
            getUserLevel(userId) { level ->
                textGuestPlayer.text = "Guest: You (Lv. ${level ?: "?"})"
            }

            if (hostId != null) {
                getUserLevel(hostId) { level ->
                    textHostPlayer.text = "Host: Connected (Lv. ${level ?: "?"})"
                }
            }
        }
    }


    // Function to get user level from Firestore (if not found, 1 is the default value)
    fun getUserLevel(uid: String, onResult: (Int?) -> Unit) {
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val level = document.getLong("level")?.toInt() ?: 1
                    onResult(level)
                } else {
                    onResult(1)
                }
            }
            .addOnFailureListener {
                onResult(1)
            }
    }
}