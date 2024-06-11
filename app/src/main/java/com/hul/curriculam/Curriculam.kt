package com.hul.curriculam

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.hul.R
import com.hul.databinding.ActivityCurriculamBinding

class Curriculam : AppCompatActivity() {

private lateinit var binding: ActivityCurriculamBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

     binding = ActivityCurriculamBinding.inflate(layoutInflater)
     setContentView(binding.root)

        val bundle = Bundle()
        bundle.putString("projectInfo",intent.getStringExtra("projectInfo"))
        findNavController(R.id.nav_host_fragment_curriculam).setGraph(R.navigation.nav_graph2)
        findNavController(R.id.nav_host_fragment_curriculam).navigate(R.id.schoolCodeFragment, bundle)

    }
}