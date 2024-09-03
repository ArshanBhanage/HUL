package com.hul.salg

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.ui.AppBarConfiguration
import com.hul.R
import com.hul.databinding.ActivityDashboardBinding
import com.hul.databinding.ActivitySalgDashboardBinding

class SalgDashboard : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivitySalgDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySalgDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}