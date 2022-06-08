package com.eachilin.imagecut

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.eachilin.imagecut.databinding.FragmentForgotPasswordOverlayBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private const val TAG = "ForgotPasswordOverlay"
class ForgotPasswordOverlay : DialogFragment() {

    private lateinit var _binding : FragmentForgotPasswordOverlayBinding
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentForgotPasswordOverlayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnResetEmail.setOnClickListener {

            val emailAddress: String = binding.etResetPassword.text.toString().trim()

            if(emailAddress.isNotEmpty() && emailAddress.contains("@")) {
                Firebase.auth.sendPasswordResetEmail(emailAddress)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Email sent.")
                            Toast.makeText(view.context, "Email has been sent", Toast.LENGTH_LONG).show()
                        } else {
                            Log.d(TAG, "Email can not be found")
                        }
                    }
            }else{
                Toast.makeText(context,"Invalid Email", Toast.LENGTH_LONG).show()
            }
            this.dismiss()
        }
    }


    companion object {

        fun newInstance(param1: String, param2: String) =
            ForgotPasswordOverlay().apply {

            }
    }
}