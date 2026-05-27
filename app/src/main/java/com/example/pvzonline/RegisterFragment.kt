package com.example.pvzonline

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class RegisterFragment : Fragment(R.layout.fragment_register) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailInput = view.findViewById<EditText>(R.id.emailInputRegister)
        val usernameInput = view.findViewById<EditText>(R.id.usernameInputRegister)
        val passwordInput = view.findViewById<EditText>(R.id.passwordInputRegister)
        val passwordConfirmInput = view.findViewById<EditText>(R.id.passwordConfirmInputRegister)
        val submitRegisterBtn = view.findViewById<Button>(R.id.submitRegisterBtn)

        submitRegisterBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = passwordConfirmInput.text.toString().trim()

            onRegisterClicked(email, username, password, confirmPassword)
        }
    }

    private fun onRegisterClicked(email: String, username: String, password: String, confirmPassword : String) {
        // Check if email and username fields are filled
        if(email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Password validation
        if(password != confirmPassword) {
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }
        if(password.length < 6) {
            Toast.makeText(requireContext(), "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
            return
        }

        // Email validation
        if(android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // Call function from register activity
            (activity as? AuthActivity)?.registerUser(email, username, password)
        }
        else {
            Toast.makeText(requireContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show()
            return
        }
    }
}
