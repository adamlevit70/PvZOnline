package com.example.pvzonline

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class LoginFragment : Fragment(R.layout.fragment_login) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailInput = view.findViewById<EditText>(R.id.emailInputLogin)
        val passwordInput = view.findViewById<EditText>(R.id.passwordInputLogin)
        val loginButton = view.findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            onLoginClicked(email, password)
        }
    }

    private fun onLoginClicked(email: String, password: String) {
        // Check if email and username fields are filled
        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this.context, "Please enter all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Password validation
        if(password.length < 6) {
            Toast.makeText(this.context, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
            return
        }

        // Email validation
        if(android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            (activity as? AuthActivity)?.loginUser(email, password)
        }
        else {
            Toast.makeText(this.context, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            return
        }
    }
}