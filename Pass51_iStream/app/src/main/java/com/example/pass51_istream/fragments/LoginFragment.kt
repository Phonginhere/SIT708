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
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())

        // TODO: find views and set up click listeners
        val usernameInput = view.findViewById<EditText>(R.id.etUsername)
        val passwordInput = view.findViewById<EditText>(R.id.etPassword)
        val loginButton = view.findViewById<Button>(R.id.btnLogin)
        val registerButton = view.findViewById<Button>(R.id.btnRegister)

        loginButton.setOnClickListener {
            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()
            if (username.isBlank() || password.isBlank()) {
                usernameInput.error = "Username cannot be empty"
                passwordInput.error = "Password cannot be empty"
                return@setOnClickListener
            }else {
                lifecycleScope.launch {
                    val userDao = db.userDao()
                    val user = userDao.getUser(username, password)
                    if (user == null) {
                        Toast.makeText(requireContext(), "Invalid username or password", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
                        val homeFragment = HomeFragment()
                        val bundle = Bundle()
                        bundle.putInt("userId", user.id)
                        homeFragment.arguments = bundle
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, homeFragment)
                            .commit()
                    }
                }
            }
            }
        registerButton.setOnClickListener {
            // Navigate to RegisterFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, SignUpFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}