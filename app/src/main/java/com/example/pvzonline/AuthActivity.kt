package com.example.pvzonline

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        enableEdgeToEdge()
        setContentView(R.layout.activity_auth)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val changeToRegisterBtn = findViewById<Button>(R.id.changeToRegisterBtn)
        val changeToLoginBtn = findViewById<Button>(R.id.changeToLoginBtn)

        changeToRegisterBtn.setOnClickListener {
            changeToRegisterFragment()
        }
        changeToLoginBtn.setOnClickListener {
            changeToLoginFragment()
        }
    }

    fun changeToRegisterFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.authFragmentContainer, RegisterFragment()).commit()
    }
    fun changeToLoginFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.authFragmentContainer, LoginFragment()).commit()
    }

    fun registerUser(email : String, username : String, password : String) {
        val auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val myUser = MyUser(
                            username, 1, 0
                        )
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(user.uid).set(myUser)
                            .addOnSuccessListener {
                                Toast.makeText(this, "User created successfully", Toast.LENGTH_LONG).show()

                                val navigationActivityIntent = Intent(this, NavigationActivity::class.java)
                                startActivity(navigationActivityIntent)
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                else {
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        Toast.makeText(this, "An account with this email already exists.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Failed to create user: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    fun loginUser(email : String, password : String) {
        val auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if(task.isSuccessful) {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                    val navigationActivityIntent = Intent(this, NavigationActivity::class.java)
                    startActivity(navigationActivityIntent)
                }
                else {
                    Toast.makeText(this, "Failed to login", Toast.LENGTH_SHORT).show()
                }
            }
    }
}