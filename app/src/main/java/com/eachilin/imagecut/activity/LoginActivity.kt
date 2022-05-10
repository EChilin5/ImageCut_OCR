package com.eachilin.imagecut.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.eachilin.imagecut.ForgotPasswordOverlay
import com.eachilin.imagecut.R
import com.eachilin.imagecut.SetUpProfile
import com.eachilin.imagecut.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "LoginActivity"
class LoginActivity : AppCompatActivity() {

    private  lateinit var binding: ActivityLoginBinding

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnSignUp: Button
    private lateinit var tvForgotPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()

        val auth = FirebaseAuth.getInstance()
        if(auth.currentUser != null){
            goHomeActivity()
        }


        btnLogin.setOnClickListener {
            btnLogin.isEnabled = false
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if(email.isBlank() || password.isBlank()){
                Toast.makeText(this, "Email password can not be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val auth = FirebaseAuth.getInstance()
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task->
                btnLogin.isEnabled = true
                if(task.isSuccessful){
                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                    goHomeActivity()
                }else{
                    Log.e(TAG, "failed to sign in", task.exception)
                    Toast.makeText(this, "Could not login", Toast.LENGTH_SHORT).show()
                }
            }

        }

        btnSignUp.setOnClickListener {
            openSignUp()
        }

        binding.tvHelp.setOnClickListener {
            openFoodItem()
        }
    }

    private fun openFoodItem() {

        val dialog = ForgotPasswordOverlay()
        val fm = this.supportFragmentManager
        dialog.show(fm, "name")
    }


    private fun openSignUp() {
        var intent = Intent(this, SetUpProfile::class.java)
        startActivity(intent)
        finish()
    }

    private fun goHomeActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()

    }

    private fun initView() {
        etEmail = binding.etUserName
        etPassword = binding.etPasword
        btnLogin = binding.btnLogin
        btnSignUp = binding.btnSignup
        tvForgotPassword = binding.tvHelp
    }
}