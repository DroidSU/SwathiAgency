package com.sujoy.swathiagency.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sujoy.swathiagency.R
import com.sujoy.swathiagency.adapters.CustomersRecyclerAdapter
import com.sujoy.swathiagency.data.CustomerModel
import com.sujoy.swathiagency.databinding.ActivityCustomerSelectionBinding
import com.sujoy.swathiagency.interfaces.OnRecyclerItemClickedListener
import com.sujoy.swathiagency.interfaces.OnSubmitButtonTapped
import com.sujoy.swathiagency.network.NetworkRepository
import com.sujoy.swathiagency.utilities.Constants
import com.sujoy.swathiagency.utilities.UtilityMethods
import com.sujoy.swathiagency.viewmodels.CsvViewModelFactory
import com.sujoy.swathiagency.viewmodels.CustomerSelectionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomerSelectionActivity : AppCompatActivity(), OnRecyclerItemClickedListener,
    OnSubmitButtonTapped {

    private lateinit var binding: ActivityCustomerSelectionBinding

    private lateinit var customerRecyclerAdapter: CustomersRecyclerAdapter
    private var customerList: MutableList<CustomerModel> = mutableListOf()
    private var selectedCustomer: CustomerModel? = null
    private var billNumber = MutableLiveData<Long>()
    private var billDialog: BillNumberDialog? = null
    private var filterBy: Int = 0
    private var salesmanName: String = ""
    private var billId: String = ""

    private lateinit var lottieOverlayFragment: LottieOverlayFragment


    private val viewModel: CustomerSelectionViewModel by viewModels {
        CsvViewModelFactory(NetworkRepository(this))
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

        binding.searchViewChooseCustomer.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    customerRecyclerAdapter.updateData(customerList)
                } else {
                    var filteredList = listOf<CustomerModel>()
                    filteredList = if (filterBy == 0) {
                        customerList.filter {
                            it.customerName.contains(
                                newText,
                                ignoreCase = true
                            )
                        }
                    } else {
                        customerList.filter {
                            it.customerRoute.contains(
                                newText,
                                ignoreCase = true
                            )
                        }
                    }
                    customerRecyclerAdapter.updateData(filteredList)
                }
                return true
            }

        })

        lifecycleScope.launch {
            viewModel.csvData.collect { data ->
                if (data.isNotEmpty()) {
                    customerList = data
                    customerRecyclerAdapter.updateData(customerList)
                    binding.llLoadingView.visibility = View.GONE
                    binding.rvCustomers.visibility = View.VISIBLE
                }
            }
        }

        billNumber.observe(this) { value ->
            if (value > 0) {
                billId = UtilityMethods.getBillId(this)!!
                binding.tvBillNumber.text = "Bill No. : $billId - $value"
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

        binding.ivFilter.setOnClickListener {
            showPopupMenu(it)
        }
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.popup_menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.menu_opt_1 -> {
                    filterBy = 0
                    binding.searchViewChooseCustomer.queryHint = "Search by customer name"
                    true
                }

                R.id.menu_opt_2 -> {
                    filterBy = 1
                    binding.searchViewChooseCustomer.queryHint = "Search by route"
                    true
                }

                else -> false
            }
        }
        popup.show()
    }

    override fun onStart() {
        super.onStart()
        fetchCustomerList()

        if (UtilityMethods.getBillNumber(this) != 0L) {
            salesmanName = UtilityMethods.getSalesmanName(this)
            billNumber.value = UtilityMethods.getBillNumber(this)
        } else {
            showBillDialog()
        }
    }

    private fun fetchCustomerList() {
        if(UtilityMethods.isNetworkAvailable(this)){
            viewModel.loadCsvData("https://drive.google.com/uc?export=download&id=${Constants.CUSTOMER_FILE_DRIVE_ID}")
        }
        else{
            Toast.makeText(
                this,
                "No internet connection available!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showBillDialog() {
        billDialog = BillNumberDialog(this)
        billDialog?.isCancelable = false
        billDialog?.show(supportFragmentManager, "bill_dialog")
    }

    override fun onCustomerClicked(customer: CustomerModel) {
        selectedCustomer = customer
        binding.btnNextScreen.visibility = View.VISIBLE
    }

    override fun onSubmitButtonTap(billId: String, billNumber: Long, salesmanSelected: String) {
        salesmanName = salesmanSelected
        billDialog?.dismiss()
        UtilityMethods.setBillNumber(this, billNumber, billId)
        UtilityMethods.setSalesmanName(this, salesmanSelected)
        this.billId = billId
        this.billNumber.value = billNumber
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.backup -> {
                uploadCurrentBackup()
                true
            }
//            R.id.action_about -> {
//                // Handle about action
//                true
//            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun uploadCurrentBackup() {
        lottieOverlayFragment.show(supportFragmentManager, "lottie_overlay")
        val lastCSVFile = UtilityMethods.getLastBackupFile(this)
        try{
            if (UtilityMethods.isNetworkAvailable(this@CustomerSelectionActivity)) {
                if(lastCSVFile != null && lastCSVFile.exists()){
                    lifecycleScope.launch (Dispatchers.IO) {
                        val uploadURI = UtilityMethods().uploadCsvFile(this@CustomerSelectionActivity, lastCSVFile)
                        if(uploadURI != null){
                            withContext(Dispatchers.Main) {
                                lottieOverlayFragment.dismiss()
                                UtilityMethods.setBillNumber(
                                    this@CustomerSelectionActivity,
                                    billNumber = (UtilityMethods.getBillNumber(this@CustomerSelectionActivity) + 1),
                                    UtilityMethods.getBillId(this@CustomerSelectionActivity)
                                )
                                Toast.makeText(
                                    this@CustomerSelectionActivity,
                                    "Order backed up successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        else{
                            withContext(Dispatchers.Main) {
                                lottieOverlayFragment.dismiss()
                                Toast.makeText(this@CustomerSelectionActivity, "File already exists", Toast.LENGTH_SHORT).show()
                            }
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

            lifecycleScope.launch(Dispatchers.IO) {

            }
        }
        catch (ex : Exception){
            Log.e("ERROR", "uploadCurrentBackup: $ex", )
            lottieOverlayFragment.dismiss()
        }
    }
}