package com.smartvisions.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var forgotPassword: TextView
    private lateinit var statusMessage: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        setContentView(R.layout.activity_main)

        // Initialize views
        initializeViews()

        // Set up click listeners
        setupClickListeners()
    }

    private fun initializeViews() {
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        loginButton = findViewById(R.id.login_button)
        registerButton = findViewById(R.id.register_button)
        forgotPassword = findViewById(R.id.forgot_password)
        statusMessage = findViewById(R.id.status_message)
    }

    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            handleLogin()
        }

        registerButton.setOnClickListener {
            handleRegister()
        }

        forgotPassword.setOnClickListener {
            handleForgotPassword()
        }
    }

    private fun handleLogin() {
        val email = emailInput.text?.toString()?.trim() ?: ""
        val password = passwordInput.text?.toString() ?: ""

        if (email.isEmpty()) {
            showMessage("Veuillez saisir votre email", true)
            return
        }

        if (password.isEmpty()) {
            showMessage("Veuillez saisir votre mot de passe", true)
            return
        }

        // Désactiver les boutons pendant l'authentification
        loginButton.isEnabled = false
        registerButton.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                loginButton.isEnabled = true
                registerButton.isEnabled = true
                if (task.isSuccessful) {
                    showMessage("Connexion réussie ! Redirection vers l'interface OCR...", false)
                    Toast.makeText(this, "✅ Connexion réussie !", Toast.LENGTH_SHORT).show()

                    // Navigate to image processing activity
                    val intent = Intent(this, ImageProcessingActivity::class.java)
                    startActivity(intent)
                    finish() // Close login activity
                } else {
                    showMessage("Erreur de connexion : ${task.exception?.message}", true)
                }
            }
    }

    private fun handleRegister() {
        val email = emailInput.text?.toString()?.trim() ?: ""
        val password = passwordInput.text?.toString() ?: ""

        if (email.isEmpty()) {
            showMessage("Veuillez saisir votre email", true)
            return
        }

        if (password.length < 6) {
            showMessage("Le mot de passe doit contenir au moins 6 caractères", true)
            return
        }

        // Désactiver les boutons pendant l'authentification
        loginButton.isEnabled = false
        registerButton.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                loginButton.isEnabled = true
                registerButton.isEnabled = true
                if (task.isSuccessful) {
                    // Sauvegarder les données utilisateur dans Realtime Database
                    val user = auth.currentUser
                    if (user != null) {
                        val userRef = database.getReference("users").child(user.uid)
                        val userData = mapOf(
                            "email" to email,
                            "createdAt" to System.currentTimeMillis()
                        )
                        userRef.setValue(userData)
                            .addOnSuccessListener {
                                showMessage("Inscription réussie ! Données sauvegardées.", false)
                                Toast.makeText(this, "✅ Inscription réussie !", Toast.LENGTH_LONG).show()
                            }
                            .addOnFailureListener { e ->
                                showMessage("Inscription réussie, mais erreur sauvegarde : ${e.message}", true)
                            }
                    } else {
                        showMessage("Inscription réussie ! Vous pouvez maintenant vous connecter", false)
                        Toast.makeText(this, "✅ Inscription réussie !", Toast.LENGTH_LONG).show()
                    }
                } else {
                    showMessage("Erreur d'inscription : ${task.exception?.message}", true)
                }
            }
    }

    private fun handleForgotPassword() {
        val email = emailInput.text?.toString()?.trim() ?: ""

        if (email.isEmpty()) {
            showMessage("Veuillez saisir votre email pour réinitialiser le mot de passe", true)
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showMessage("Un email de réinitialisation a été envoyé à $email", false)
                    Toast.makeText(this, "📧 Email de réinitialisation envoyé !", Toast.LENGTH_LONG).show()
                } else {
                    showMessage("Erreur : ${task.exception?.message}", true)
                }
            }
    }

    private fun showMessage(message: String, isError: Boolean) {
        statusMessage.text = message
        statusMessage.setTextColor(if (isError) getColor(android.R.color.holo_red_dark)
                                  else getColor(android.R.color.holo_green_dark))
        statusMessage.visibility = View.VISIBLE
    }
}