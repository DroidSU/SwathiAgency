package com.sujoy.swathiagency.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sujoy.swathiagency.R
import com.sujoy.swathiagency.adapters.CustomerOrderRecyclerAdapter
import com.sujoy.swathiagency.data.datamodels.CustomerOrderModel
import com.sujoy.swathiagency.data.datamodels.OrdersTable
import com.sujoy.swathiagency.database.AppDatabase
import com.sujoy.swathiagency.databinding.ActivityViewOrdersBinding
import com.sujoy.swathiagency.utilities.Constants
import com.sujoy.swathiagency.utilities.DatabaseRepository
import com.sujoy.swathiagency.utilities.UtilityMethods
import com.sujoy.swathiagency.viewmodels.ViewOrderVMFactory
import com.sujoy.swathiagency.viewmodels.ViewOrderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.roundToLong

class ViewOrdersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewOrdersBinding

    private var ordersArrayList: MutableList<OrdersTable> = mutableListOf()
    private lateinit var adapter: CustomerOrderRecyclerAdapter
    private lateinit var lottieOverlayFragment: LottieOverlayFragment
    private var viewType = MutableLiveData(1)

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
        adapter = CustomerOrderRecyclerAdapter(ordersArrayList)
        binding.rvOrders.adapter = adapter


        binding.ctvShowAll.setOnClickListener {
            viewModel.setViewType(0)
        }

        binding.ctvShowItc.setOnClickListener {
            viewModel.setViewType(1)
        }

        binding.ctvShowAvt.setOnClickListener {
            viewModel.setViewType(2)
        }

        binding.ivMoreOptions.setOnClickListener {
            showPopupMenu()
        }

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.ordersList.collect { data ->
                withContext(Dispatchers.Main) {
                    binding.lvViewOrderLoading.visibility = View.GONE
                    binding.clViewOrderMain.visibility = View.VISIBLE
                    if (data.isNotEmpty()) {
                        ordersArrayList = data
                        adapter.updateData(ordersArrayList)
                        binding.rvOrders.visibility = View.VISIBLE
                        binding.llTotalView.visibility = View.VISIBLE
                        binding.tvAllOrdersBackedUp.visibility = View.GONE
                    } else {
                        binding.rvOrders.visibility = View.GONE
                        binding.llTotalView.visibility = View.GONE
                        binding.tvAllOrdersBackedUp.visibility = View.VISIBLE
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

        lifecycleScope.launch {
            viewModel.viewType.observe(this@ViewOrdersActivity) { value ->
                viewType.value = value
                updateDataOnUI()
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.uploadSuccess.collect() { isSuccess ->
                withContext(Dispatchers.Main) {
                    if (lottieOverlayFragment.isAdded) {
                        lottieOverlayFragment.dismiss()

                        if (isSuccess) {
                            Toast.makeText(
                                this@ViewOrdersActivity,
                                "Backup successful",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@ViewOrdersActivity,
                                "Backup failed",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        updateDataOnUI()
                    }
                }
            }
        }
    }

    private fun showPopupMenu() {
        val popupMenu = PopupMenu(this, binding.ivMoreOptions)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.backup_itc -> {
                    backupOrders(Constants.COMPANY_TYPE_ITC)
                    backupTotalOrder()
                    true
                }

                R.id.backup_avt -> {
                    backupOrders(Constants.COMPANY_TYPE_AVT)
                    backupTotalOrder()
                    true
                }

                R.id.backup_all -> {
//                    backupOrders("")
//                    backupTotalOrder()
                    true
                }

                else -> false
            }
        }

        // Show the PopupMenu
        popupMenu.show()
    }

    private fun updateDataOnUI() {
        when (viewType.value) {
            0 -> {
                binding.ctvShowAll.isChecked = true
                binding.ctvShowItc.isChecked = false
                binding.ctvShowAvt.isChecked = false
                viewModel.getAllOrderObjects()
            }

            1 -> {
                binding.ctvShowItc.isChecked = true
                binding.ctvShowAvt.isChecked = false
                binding.ctvShowAll.isChecked = false
                viewModel.getCompanyOrderObject(Constants.COMPANY_TYPE_ITC)
            }

            2 -> {
                binding.ctvShowAvt.isChecked = true
                binding.ctvShowItc.isChecked = false
                binding.ctvShowAll.isChecked = false
                viewModel.getCompanyOrderObject(Constants.COMPANY_TYPE_AVT)
            }
        }

    }

    override fun onStart() {
        super.onStart()
        updateDataOnUI()
    }

    private fun backupOrders(companyType: String) {
        if (UtilityMethods.isNetworkAvailable(this)) {
            lottieOverlayFragment.show(supportFragmentManager, "lottie_overlay")
            lifecycleScope.launch(Dispatchers.IO) {
                if (companyType == "") {
                    viewModel.backupOrders(this@ViewOrdersActivity, ordersArrayList)
                } else {
                    viewModel.backupOrders(
                        this@ViewOrdersActivity,
                        ordersArrayList.filter { it.companyName == companyType }.toList()
                    )
                }
            }
        } else {
            Toast.makeText(this, "No network available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun backupTotalOrder() {
        if (UtilityMethods.isNetworkAvailable(this)) {
            val customerOrderTotalList: MutableList<CustomerOrderModel> = mutableListOf()
            var totalValue = 0L
            var orderList = ordersArrayList.toList()
            if(viewType.value == 1){
                orderList = orderList.filter { it.companyName == Constants.COMPANY_TYPE_ITC }
            }
            else if(viewType.value == 2){
                orderList = orderList.filter { it.companyName == Constants.COMPANY_TYPE_AVT }
            }

            for (order in orderList) {
                customerOrderTotalList.add(
                    CustomerOrderModel(
                        order.orderId,
                        order.customerName,
                        order.orderTotal,
                        order.createdDate,
                        order.companyName
                    )
                )
                totalValue += order.orderTotal.roundToLong()
            }

            viewModel.backupCustomerOrderTotal(this, customerOrderTotalList, totalValue)
        } else {
            Toast.makeText(this, "Network not available", Toast.LENGTH_SHORT).show()
        }
    }
}