package com.hul.screens.field_auditor_dashboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import com.hul.databinding.ActivityDashboardAuditorBinding
import com.hul.databinding.ActivityDashboardBinding

class FieldAuditorDashboard : AppCompatActivity(),DashboardInterface {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDashboardAuditorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardAuditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun setTitle(title: String) {
    }
}