package com.example.pvzonline

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userEmail = view.findViewById<TextView>(R.id.userEmail)
        val userLevel = view.findViewById<TextView>(R.id.userLevel)
        val userXp = view.findViewById<TextView>(R.id.userXp)
        val changeUsernameInput = view.findViewById<EditText>(R.id.changeUsernameInput)
        val saveChangesBtn = view.findViewById<Button>(R.id.saveChangesBtn)

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        userEmail.text = user!!.email.toString()
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(user.uid).get().addOnSuccessListener {
            val myUser = it.toObject(MyUser::class.java)
            userLevel.text = "Level: " + myUser!!.level.toString()
            userXp.text = "XP: " + myUser!!.xp.toString()
            changeUsernameInput.setText(myUser!!.username)
        }

        saveChangesBtn.setOnClickListener {
            val level = userLevel.text.toString()
            val xp = userXp.text.toString()
            val username = changeUsernameInput.text.toString()
            //val email = emailUser.text.toString()

            val myUser = MyUser(username, level.toInt(), xp.toInt())
            db.collection("users").document(user.uid).set(myUser)

            Toast.makeText(requireContext(), "Changes saved", Toast.LENGTH_LONG).show()
        }
    }
}