package com.eachilin.imagecut.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.eachilin.imagecut.R
import com.eachilin.imagecut.databinding.ActivityMainBinding
import com.eachilin.imagecut.fragment.HomeFragment
import com.eachilin.imagecut.fragment.SettingFragment


class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val HomeFragment = HomeFragment()
        val SettingFragment = SettingFragment()

        openFragment(HomeFragment)

        binding.bottomNavBar.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.ic_Home ->openFragment(HomeFragment)
                R.id.ic_Setting -> openFragment(SettingFragment)
            }
            true
        }

    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fl_menu, fragment)
            commit()
        }
    }


}