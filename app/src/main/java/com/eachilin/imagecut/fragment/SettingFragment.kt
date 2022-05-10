package com.eachilin.imagecut.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.eachilin.imagecut.R
import com.eachilin.imagecut.activity.LoginActivity
import com.eachilin.imagecut.databinding.FragmentSettingBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.NonDisposableHandle.parent


class SettingFragment : Fragment() {


    private var _binding : FragmentSettingBinding? = null
    private val binding get() = _binding!!

    private lateinit var btnLogOut : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSettingBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnLogOut = binding.btnSignOut
        btnLogOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent  = Intent(context, LoginActivity::class.java)
            context?.startActivity(intent)
            activity?.finish()
        }

    }

}