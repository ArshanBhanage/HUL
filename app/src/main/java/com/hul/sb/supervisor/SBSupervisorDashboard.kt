package com.hul.sb.supervisor

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.ui.AppBarConfiguration
import com.hul.R
import com.hul.databinding.ActivitySBMobiliserDashboardBinding
import com.hul.databinding.ActivitySbsupervisorDashboardBinding

class SBSupervisorDashboard : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivitySbsupervisorDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySbsupervisorDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}