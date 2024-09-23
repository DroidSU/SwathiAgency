package com.sujoy.swathiagency.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sujoy.swathiagency.R
import com.sujoy.swathiagency.databinding.ActivityMainBinding
import com.sujoy.swathiagency.utilities.UtilityMethods

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if(UtilityMethods.getSalesmanName(this).isEmpty()){
            startActivity(Intent(this, SalesmanSelectionActivity::class.java))
            finish()
        }
        else{
            startActivity(Intent(this, CustomerSelectionActivity::class.java))
            finish()
        }
    }
}