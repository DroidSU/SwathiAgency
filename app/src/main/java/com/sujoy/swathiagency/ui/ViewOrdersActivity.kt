package com.sujoy.swathiagency.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sujoy.swathiagency.R
import com.sujoy.swathiagency.adapters.CustomerOrderRecyclerAdapter
import com.sujoy.swathiagency.data.datamodels.CustomerOrderModel
import com.sujoy.swathiagency.database.AppDatabase
import com.sujoy.swathiagency.databinding.ActivityViewOrdersBinding
import com.sujoy.swathiagency.utilities.UtilityMethods
import com.sujoy.swathiagency.viewmodels.DatabaseRepository
import com.sujoy.swathiagency.viewmodels.ViewOrderVMFactory
import com.sujoy.swathiagency.viewmodels.ViewOrderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode

class ViewOrdersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewOrdersBinding

    private var customerOrderModelArrayList: MutableList<CustomerOrderModel> = mutableListOf()
    private lateinit var adapter: CustomerOrderRecyclerAdapter
    private lateinit var lottieOverlayFragment: LottieOverlayFragment

    private val viewModel: ViewOrderViewModel by viewModels {
        ViewOrderVMFactory(DatabaseRepository(AppDatabase.getDatabase(this).orderDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityViewOrdersBinding.inflate(layoutInflater)
        val root = binding.root
        setContentView(root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lottieOverlayFragment = LottieOverlayFragment.newInstance()

        binding.rvOrders.layoutManager = LinearLayoutManager(this)
//        binding.rvOrders.addItemDecoration(SpacesItemDecoration(16))
        adapter = CustomerOrderRecyclerAdapter(customerOrderModelArrayList)
        binding.rvOrders.adapter = adapter

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.customerOrderList.collect { data ->
                withContext(Dispatchers.Main) {
                    if (data.isNotEmpty()) {
                        customerOrderModelArrayList = data
                        adapter.updateData(customerOrderModelArrayList)
                        binding.lvViewOrderLoading.visibility = View.GONE
                        binding.clViewOrderMain.visibility = View.VISIBLE
                    }
                }
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.totalOrderValue.collect { value ->
                withContext(Dispatchers.Main) {
                    binding.tvTotalValue.text =
                        BigDecimal(value.toString()).setScale(2, RoundingMode.HALF_UP).toFloat()
                            .toString()
                }
            }
        }

        binding.ivBackup.setOnClickListener {
            if(customerOrderModelArrayList.isNotEmpty()){
                lottieOverlayFragment.show(supportFragmentManager, "lottie_overlay")
                viewModel.backupCustomerOrderTotal(this, customerOrderModelArrayList)
            }
        }

        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.uri.observe(this@ViewOrdersActivity, Observer { data ->
                lottieOverlayFragment.dismiss()
                if (data != null) {
                    Toast.makeText(this@ViewOrdersActivity, "Backup Successful", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(this@ViewOrdersActivity, "Backup Failed", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.getAllOrderObjects(UtilityMethods.getCurrentDateString("dd-MM-yyyy"))
    }
}