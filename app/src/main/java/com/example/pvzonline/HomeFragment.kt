package com.example.pvzonline

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val roomsRef = db.collection("rooms")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnStartGame = view.findViewById<Button>(R.id.btnStartGame)
        val btnCreateRoom = view.findViewById<Button>(R.id.btnCreateRoom)
        val btnJoinRoom = view.findViewById<Button>(R.id.btnJoinRoom)

        btnStartGame.setOnClickListener {
            val gameActivityIntent = Intent(activity, GameActivity::class.java)
            startActivity(gameActivityIntent)
        }

        btnCreateRoom.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            createRoom(auth.currentUser!!.uid)
        }

        btnJoinRoom.setOnClickListener {
            (activity as? NavigationActivity)?.switchToJoinRoomFragment()
        }
    }

    fun createRoom(currentUserId: String) {
        fun tryCreate() {
            val code = generateRoomCode()
            val roomRef = roomsRef.document(code)

            roomRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    tryCreate()
                } else {
                    val roomData = hashMapOf(
                        "hostId" to currentUserId,
                        "guestId" to null,
                        "gameStarted" to false
                    )

                    roomRef.set(roomData)
                        .addOnSuccessListener {
                            // After room is created, go to waiting room
                            Toast.makeText(requireContext(), "Room created", Toast.LENGTH_SHORT).show()
                            (activity as? NavigationActivity)?.goToWaitingRoom(code)
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to create room", Toast.LENGTH_LONG).show()
                        }
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Firestore error", Toast.LENGTH_LONG).show()
            }
        }

        tryCreate()
    }


    // Generates 6 chars long code to join the hosted room
    fun generateRoomCode(): String {
        val length : Int = 6
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }
}