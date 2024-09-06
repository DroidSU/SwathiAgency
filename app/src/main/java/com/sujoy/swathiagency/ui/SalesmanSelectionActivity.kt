package com.sujoy.swathiagency.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.MutableLiveData
import com.sujoy.swathiagency.R
import com.sujoy.swathiagency.databinding.ActivitySalesmanSelectionBinding
import com.sujoy.swathiagency.utilities.UtilityMethods

class SalesmanSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySalesmanSelectionBinding
    private var selectedSalesmanName = MutableLiveData<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        binding = ActivitySalesmanSelectionBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.tvSalesman1.text = "KK"
        binding.tvSalesman2.text = "MADHAN"
        binding.tvSalesman3.text = "NANDHA"

        binding.llSalesman1.setOnClickListener {
            selectedSalesmanName.value = "KK"
        }

        binding.llSalesman2.setOnClickListener {
            selectedSalesmanName.value = "MADHAN"
        }

        binding.llSalesman3.setOnClickListener {
            selectedSalesmanName.value = "NANDHA"
        }

        selectedSalesmanName.observe (this) { value ->
            if(value.isNotEmpty()){
                if(value.equals("KK", true)){
                    binding.llSalesman1.background = ContextCompat.getDrawable(this, R.drawable.rounded_corner_black_border)

                    binding.llSalesman2.background = ContextCompat.getDrawable(this, R.drawable.rounded_corner_no_border)
                    binding.llSalesman3.background = ContextCompat.getDrawable(this, R.drawable.rounded_corner_no_border)
                }
                else if(value.equals("MADHAN", true)){
                    binding.llSalesman2.background = ContextCompat.getDrawable(this, R.drawable.rounded_corner_black_border)

                    binding.llSalesman1.background = ContextCompat.getDrawable(this, R.drawable.rounded_corner_no_border)
                    binding.llSalesman3.background = ContextCompat.getDrawable(this, R.drawable.rounded_corner_no_border)
                }
                else {
                    binding.llSalesman3.background = ContextCompat.getDrawable(this, R.drawable.rounded_corner_black_border)

                    binding.llSalesman1.background = ContextCompat.getDrawable(this, R.drawable.rounded_corner_no_border)
                    binding.llSalesman2.background = ContextCompat.getDrawable(this, R.drawable.rounded_corner_no_border)
                }

                binding.btnNextScreen.visibility = View.VISIBLE
            }
            else{
                binding.btnNextScreen.visibility = View.GONE
            }
        }

        binding.btnNextScreen.setOnClickListener {
            if(selectedSalesmanName.value.toString().isNotEmpty()){
                UtilityMethods.setSalesmanName(this, selectedSalesmanName.value.toString())
                startActivity(Intent(this, CustomerSelectionActivity::class.java))
                finish()
            }
            else{
                Toast.makeText(this, "Please choose salesman", Toast.LENGTH_SHORT).show()
            }
        }
    }
}