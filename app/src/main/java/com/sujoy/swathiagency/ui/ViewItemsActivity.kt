package com.sujoy.swathiagency.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.sujoy.swathiagency.R
import com.sujoy.swathiagency.adapters.CompanyViewPagerAdapter
import com.sujoy.swathiagency.data.datamodels.CustomerModel
import com.sujoy.swathiagency.databinding.ActivityViewItemsBinding

class ViewItemsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewItemsBinding

    private var selectedCustomer: CustomerModel? = null
    private lateinit var viewPagerAdapter: CompanyViewPagerAdapter
    private var currentItem = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityViewItemsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_parent_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Code that you need to execute on back press, e.g. finish()
                startActivity(Intent(this@ViewItemsActivity, CustomerSelectionActivity::class.java))
                finish()
            }
        })

        selectedCustomer = intent.getParcelableExtra("customer_model")
        currentItem = intent.getIntExtra("current_item", 0)

        viewPagerAdapter = selectedCustomer?.let { CompanyViewPagerAdapter(this, it) }!!
        binding.viewpagerCompany.adapter = viewPagerAdapter
        binding.viewpagerCompany.setCurrentItem(currentItem, true)

        TabLayoutMediator(
            binding.tlCompanyTabs,
            binding.viewpagerCompany
        ) { tab, position ->
            tab.text = when (position) {
                0 -> "ITC"
                1 -> "AVT"
                2 -> "OTHERS"
                else -> null
            }
        }.attach()

        binding.tlCompanyTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    when (tab.position) {
                        0 -> {
                            binding.viewpagerCompany.setCurrentItem(0, true)
                        }

                        1 -> {
                            binding.viewpagerCompany.setCurrentItem(1, true)
                        }

                        else -> {
                            Toast.makeText(
                                this@ViewItemsActivity,
                                "Coming Soon",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })


    }
}