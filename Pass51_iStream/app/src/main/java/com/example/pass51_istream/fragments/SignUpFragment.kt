package com.example.pass51_istream.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.pass51_istream.R
import com.example.pass51_istream.database.AppDatabase
import com.example.pass51_istream.database.User
import kotlinx.coroutines.launch

class SignUpFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())

        // TODO: find views and set up click listener
        val fullNameInput = view.findViewById<EditText>(R.id.etFullName)
        val usernameInput = view.findViewById<EditText>(R.id.etUsername)
        val passwordInput = view.findViewById<EditText>(R.id.etPassword)
        val confirmPasswordInput = view.findViewById<EditText>(R.id.etConfirmPassword)
        val registerButton = view.findViewById<Button>(R.id.btnSignup)

        registerButton.setOnClickListener {
            val fullName = fullNameInput.text.toString()
            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            if (fullName.isBlank()) {
                fullNameInput.error = "Full name cannot be empty"
                return@setOnClickListener
            }
            if (username.isBlank()) {
                usernameInput.error = "Username cannot be empty"
                return@setOnClickListener
            }
            if (password.isBlank()) {
                passwordInput.error = "Password cannot be empty"
                return@setOnClickListener
            }
            if (confirmPassword != password) {
                confirmPasswordInput.error = "Passwords do not match"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val userDao = db.userDao()
                val existingUser = userDao.getUserByUsername(username)
                if (existingUser != null) {
                    Toast.makeText(requireContext(), "Username already taken", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    val newUser =
                        User(fullName = fullName, username = username, password = password)
                    userDao.insertUser(newUser)
                    Toast.makeText(requireContext(), "Registration successful!", Toast.LENGTH_SHORT)
                        .show()
                    // Optionally, navigate to log in or home screen after registration
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, LoginFragment())
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
    }
}
