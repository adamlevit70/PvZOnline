package com.example.pvzonline

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
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
                // Start game here
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
                        onGuestJoined(hostId)
                    }

                    if (gameStarted == true) {
                        // Start game
                    }
                }
            }
    }

    fun onGuestJoined(hostId: String?) {
        hasGuest = true

        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser!!.uid

        if(hostId == userId) {
            canStartGame = true
            val btnStartGame = findViewById<Button>(R.id.btnStartGame)
            btnStartGame.visibility = View.VISIBLE
        }
    }


    fun updatePlayerTexts(hostId: String?, guestId: String?) {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser!!.uid

        val textHostPlayer = findViewById<TextView>(R.id.textHostPlayer)
        val textGuestPlayer = findViewById<TextView>(R.id.textGuestPlayer)

        if (hostId == userId) {
            textHostPlayer.text = "Host: You"
            textGuestPlayer.text = if (guestId == null) {
                "Guest: Waiting for player..."
            } else {
                "Guest: Connected"
            }
        } else {
            textHostPlayer.text = "Host: Connected"
            textGuestPlayer.text = "Guest: You"
        }
    }
}