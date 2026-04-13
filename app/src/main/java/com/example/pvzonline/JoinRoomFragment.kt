package com.example.pvzonline

import android.os.Bundle
import android.text.InputFilter
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class JoinRoomFragment : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    private val roomsRef = db.collection("rooms")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val joinRoomBtn = view.findViewById<Button>(R.id.joinRoomBtn)
        val roomCodeInput = view.findViewById<TextView>(R.id.roomCodeInput)

        joinRoomBtn.setOnClickListener {
            if(roomCodeInput.text.toString().length != 6) {
                Toast.makeText(requireContext(), "Code must be 6 chars long", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val code = roomCodeInput.text.toString()

            val auth = FirebaseAuth.getInstance()
            joinRoom(code, auth.currentUser!!.uid)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_join_room, container, false)
    }


    fun joinRoom(code: String, currentUserId: String) {
        val roomRef = roomsRef.document(code)

        roomRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                Toast.makeText(requireContext(), "Room not found", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            val guestId = document.getString("guestId")
            val gameStarted = document.getBoolean("gameStarted") ?: false

            if (guestId != null || gameStarted) {
                Toast.makeText(requireContext(), "Room is full or already started", Toast.LENGTH_LONG).show()
                return@addOnSuccessListener
            }

            roomRef.update(
                mapOf(
                    "guestId" to currentUserId
                )
            ).addOnSuccessListener {
                // Join the waiting room with the host
                Toast.makeText(requireContext(), "Joining room", Toast.LENGTH_SHORT).show()
                (activity as? NavigationActivity)?.goToWaitingRoom(code)
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to join room", Toast.LENGTH_LONG).show()
            }

        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Firestore error", Toast.LENGTH_LONG).show()
        }
    }
}