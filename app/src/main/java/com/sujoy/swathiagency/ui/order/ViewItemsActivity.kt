package com.sujoy.swathiagency.ui.order

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.sujoy.swathiagency.R
import com.sujoy.swathiagency.adapters.CompanyViewPagerAdapter
import com.sujoy.swathiagency.data.datamodels.CustomerModel
import com.sujoy.swathiagency.data.datamodels.ItemsModel
import com.sujoy.swathiagency.databinding.ActivityViewItemsBinding
import com.sujoy.swathiagency.ui.CustomerSelectionActivity

class ViewItemsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewItemsBinding

    private var selectedCustomer: CustomerModel? = null
    private lateinit var viewPagerAdapter: CompanyViewPagerAdapter
    private var isITCOrderCreated = false
    private var isAVTOrderCreated = false
    private var orderList: MutableList<ItemsModel> = mutableListOf()
    private var orderId = ""
    private var fragmentIndex = 0

    @SuppressLint("NotifyDataSetChanged")
    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val companyType = result.data?.getStringExtra("company")

                if (companyType == "ITC") {
                    isITCOrderCreated = true
                }

                if (companyType == "AVT") {
                    isAVTOrderCreated = true
                }

                if (isITCOrderCreated && isAVTOrderCreated) {
                    startActivity(
                        Intent(
                            this@ViewItemsActivity,
                            CustomerSelectionActivity::class.java
                        )
                    )
                    finish()
                } else {
                    if (isITCOrderCreated) {
                        binding.viewpagerCompany.setCurrentItem(1, true)
                    } else {
                        binding.viewpagerCompany.setCurrentItem(0, true)
                    }
                }
            }
        }

    // Method to provide a way for fragments to access the result launcher
    fun getResultLauncher() = resultLauncher

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

        if (intent.hasExtra("order_list")) {
            orderList = intent.getParcelableArrayListExtra<ItemsModel>("order_list")!!
            orderId = intent.getStringExtra("order_id")!!
            fragmentIndex = intent.getIntExtra("index", 0)
        }

        viewPagerAdapter = selectedCustomer?.let {
            CompanyViewPagerAdapter(
                this,
                it,
                orderList.filter { itemsModel -> itemsModel.companyName == "ITC" }.toMutableList(),
                orderList.filter { itemsModel -> itemsModel.companyName == "AVT" }.toMutableList(),
                orderId
            )
        }!!
        binding.viewpagerCompany.adapter = viewPagerAdapter


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

        binding.viewpagerCompany.setCurrentItem(fragmentIndex, true)
    }
}