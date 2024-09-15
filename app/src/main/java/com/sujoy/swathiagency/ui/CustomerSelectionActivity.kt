package com.sujoy.swathiagency.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
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
import com.sujoy.swathiagency.data.dbModels.OrderFileModel
import com.sujoy.swathiagency.database.AppDatabase
import com.sujoy.swathiagency.databinding.ActivityCustomerSelectionBinding
import com.sujoy.swathiagency.interfaces.OnRecyclerItemClickedListener
import com.sujoy.swathiagency.network.NetworkRepository
import com.sujoy.swathiagency.utilities.Constants
import com.sujoy.swathiagency.utilities.UtilityMethods
import com.sujoy.swathiagency.viewmodels.CsvViewModelFactory
import com.sujoy.swathiagency.viewmodels.CustomerSelectionViewModel
import com.sujoy.swathiagency.viewmodels.FileObjectModelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CustomerSelectionActivity : AppCompatActivity(), OnRecyclerItemClickedListener {

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
        CsvViewModelFactory(
            NetworkRepository(this),
            FileObjectModelRepository(AppDatabase.getDatabase(this).orderDao())
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

        lifecycleScope.launch {
            viewModel.fileList.collect { data ->
                if (data.isNotEmpty()) {
                    fileObjectModelsList = data

                    uploadBackupFiles()
                }
//                else{
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(this@CustomerSelectionActivity, "No files to back up", Toast.LENGTH_SHORT).show()
//                    }
//                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        fetchCustomerList()
    }

    private fun fetchCustomerList() {
        viewModel.getCustomerData(this)
    }


    override fun onCustomerClicked(customer: CustomerModel) {
        UtilityMethods.hideKeyBoard(binding.root, this)
        selectedCustomer = customer
        binding.btnNextScreen.visibility = View.VISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.backup_itc -> {
                companyType = Constants.COMPANY_TYPE_ITC
                viewModel.getFilesNotBackedUp(Constants.COMPANY_TYPE_ITC)
                true
            }

            R.id.backup_avt -> {
                companyType = Constants.COMPANY_TYPE_AVT
                viewModel.getFilesNotBackedUp(Constants.COMPANY_TYPE_AVT)
                true
            }

            R.id.view_orders -> {
                startActivity(Intent(this, ViewOrdersActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun uploadBackupFiles() {
        lottieOverlayFragment.show(supportFragmentManager, "lottie_overlay")
        try {
            if (UtilityMethods.isNetworkAvailable(this@CustomerSelectionActivity)) {
                lifecycleScope.launch(Dispatchers.IO) {
                    if (fileObjectModelsList.isNotEmpty()) {

                        for (fileObject in fileObjectModelsList) {
                            val file = File(fileObject.fileURI)
                            UtilityMethods().backupItemsCSVFile(
                                this@CustomerSelectionActivity,
                                file
                            )
//                            viewModel.markFilesAsBackedUp(fileObject.fileName, fileObject.companyName)
                        }

                        withContext(Dispatchers.Main) {
                            lottieOverlayFragment.dismiss()
                            Toast.makeText(
                                this@CustomerSelectionActivity,
                                "Order backed up successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } else {
                lottieOverlayFragment.dismiss()
                Toast.makeText(
                    this@CustomerSelectionActivity,
                    "No internet connection available. Order could not be backed up",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (ex: Exception) {
            Log.e("ERROR", "uploadCurrentBackup: $ex")
            lottieOverlayFragment.dismiss()
        }
    }
}