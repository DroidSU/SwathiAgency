package com.sujoy.swathiagency.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sujoy.swathiagency.R
import com.sujoy.swathiagency.adapters.CustomersRecyclerAdapter
import com.sujoy.swathiagency.data.datamodels.CustomerModel
import com.sujoy.swathiagency.data.datamodels.OrderFileModel
import com.sujoy.swathiagency.database.AppDatabase
import com.sujoy.swathiagency.databinding.ActivityCustomerSelectionBinding
import com.sujoy.swathiagency.interfaces.OnCustomerRecyclerItemClick
import com.sujoy.swathiagency.network.NetworkRepository
import com.sujoy.swathiagency.ui.collections.CashCollectionView
import com.sujoy.swathiagency.ui.order.ViewItemsActivity
import com.sujoy.swathiagency.utilities.Constants
import com.sujoy.swathiagency.utilities.DatabaseRepository
import com.sujoy.swathiagency.utilities.UtilityMethods
import com.sujoy.swathiagency.viewmodels.CustomerSelectionVMFactory
import com.sujoy.swathiagency.viewmodels.CustomerSelectionViewModel
import kotlinx.coroutines.launch

class CustomerSelectionActivity : AppCompatActivity(), OnCustomerRecyclerItemClick {

    private lateinit var binding: ActivityCustomerSelectionBinding

    private lateinit var customerRecyclerAdapter: CustomersRecyclerAdapter
    private var customerList: MutableList<CustomerModel> = mutableListOf()
    private var selectedCustomer: CustomerModel? = null
    private var fileObjectModelsList: List<OrderFileModel> = listOf()
    private var companyType = Constants.COMPANY_TYPE_ITC

    private var customerRouteArrayAdapter: ArrayAdapter<String>? = null
    private var routeStringsList: MutableList<String> = mutableListOf()
    private var selectedRouteFromList: String = ""

    private lateinit var lottieOverlayFragment: LottieOverlayFragment


    private val viewModel: CustomerSelectionViewModel by viewModels {
        CustomerSelectionVMFactory(
            NetworkRepository(this),
            DatabaseRepository(AppDatabase.getDatabase(this).orderDao())
        )
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCustomerSelectionBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lottieOverlayFragment = LottieOverlayFragment.newInstance()

        binding.rvCustomers.layoutManager = LinearLayoutManager(this)
        customerRecyclerAdapter = CustomersRecyclerAdapter(this, customerList, this)
        binding.rvCustomers.adapter = customerRecyclerAdapter

        customerRouteArrayAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, routeStringsList)
        binding.searchCustomersRouteDropdown.setAdapter(customerRouteArrayAdapter)
        selectedRouteFromList = UtilityMethods.getSelectedRoute(this)

        binding.searchCustomersRouteDropdown.setOnClickListener {
            binding.searchCustomersRouteDropdown.showDropDown()
        }

        binding.searchCustomersRouteDropdown.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                binding.searchCustomersRouteDropdown.showDropDown()
            }
        }

        binding.searchCustomersRouteDropdown.setOnItemClickListener { parent, _, position, _ ->
            UtilityMethods.hideKeyBoard(binding.searchCustomersRouteDropdown, this)
            selectedRouteFromList = parent.getItemAtPosition(position).toString()

            UtilityMethods.setSelectedRoute(this, selectedRouteFromList)
            customerRecyclerAdapter.updateData(customerList.filter {
                it.customerRoute.contains(
                    selectedRouteFromList,
                    ignoreCase = true
                )
            })
            binding.searchView.visibility = View.VISIBLE
            binding.searchCustomersRouteDropdown.clearFocus()
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(newText == null){
                    customerRecyclerAdapter.updateData(customerList)
                }
                else{
                    customerRecyclerAdapter.updateData(customerList.filter { it.customerName.contains(newText, ignoreCase = true) && it.customerRoute == selectedRouteFromList })
                }
                return true
            }
        })

        lifecycleScope.launch {
            viewModel.csvData.collect { data ->
                if (data.isNotEmpty()) {
                    customerList = data
                    customerList.sortBy { item -> item.customerName }
                    routeStringsList =
                        customerList.map { it.customerRoute }.distinct().toMutableList()
                    customerRouteArrayAdapter?.let {
                        it.clear()
                        it.addAll(routeStringsList)
                        it.notifyDataSetChanged()
                    }

                    if(selectedRouteFromList.isNotEmpty()){
                        binding.searchCustomersRouteDropdown.setText(selectedRouteFromList, false)
                        customerRecyclerAdapter.updateData(customerList.filter {
                            it.customerRoute.contains(
                                selectedRouteFromList,
                                ignoreCase = true
                            )
                        })
                    }
                    else{
                        customerRecyclerAdapter.updateData(customerList)
                    }

                    binding.searchView.visibility = View.VISIBLE
                    binding.llLoadingView.visibility = View.GONE
                    binding.rvCustomers.visibility = View.VISIBLE
                }
            }
        }

        binding.btnNextScreen.setOnClickListener {
            startActivity(
                Intent(this, ViewItemsActivity::class.java).putExtra(
                    "customer_model",
                    selectedCustomer
                )
            )
            finish()
        }

        binding.btnOpenOrders!!.setOnClickListener {
            startActivity(
                Intent(this, ViewItemsActivity::class.java).putExtra(
                    "customer_model",
                    selectedCustomer
                )
            )
            finish()
        }

        binding.btnOpenCashCollection!!.setOnClickListener {
            startActivity(
                Intent(this, CashCollectionView::class.java).putExtra(
                    "customer_model",
                    selectedCustomer
                )
            )
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        fetchCustomerList()
    }

    private fun fetchCustomerList() {
        viewModel.getCustomerData(this)
    }


    override fun onItemClicked(customer: CustomerModel) {
        UtilityMethods.hideKeyBoard(binding.root, this)
        selectedCustomer = customer

        binding.llOpenType!!.visibility = View.VISIBLE
//        binding.btnNextScreen.visibility = View.VISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.view_orders -> {
                startActivity(Intent(this, ViewOrdersActivity::class.java))
                true
            }


//            R.id.settings -> {
//                UtilityMethods.showAlertDialog(this, "Please pay Rs. 5000 to the following UPI : 6289604521@axl", {}, {
//                    return@showAlertDialog
//                })
//                true
//            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}