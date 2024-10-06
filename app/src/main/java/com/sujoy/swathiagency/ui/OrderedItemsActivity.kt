package com.sujoy.swathiagency.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sujoy.swathiagency.R
import com.sujoy.swathiagency.adapters.OrderedItemsRecyclerAdapter
import com.sujoy.swathiagency.data.datamodels.CustomerModel
import com.sujoy.swathiagency.data.datamodels.ItemsModel
import com.sujoy.swathiagency.data.datamodels.OrdersTable
import com.sujoy.swathiagency.database.AppDatabase
import com.sujoy.swathiagency.databinding.ActivityOrderedItemsBinding
import com.sujoy.swathiagency.utilities.Constants
import com.sujoy.swathiagency.utilities.DatabaseRepository
import com.sujoy.swathiagency.utilities.UtilityMethods
import com.sujoy.swathiagency.utilities.UtilityMethods.Companion.showAlertDialog
import com.sujoy.swathiagency.viewmodels.OrderedItemsVMFactory
import com.sujoy.swathiagency.viewmodels.OrderedItemsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode

class OrderedItemsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderedItemsBinding
    private var orderedItemsList: ArrayList<ItemsModel> = arrayListOf()
    private lateinit var customerModel: CustomerModel
    private var totalBillAmount: Float = 0F
    private lateinit var orderedItemsRecyclerAdapter: OrderedItemsRecyclerAdapter
    private var companyType = Constants.COMPANY_TYPE_ITC
    private var orderId: String = ""
    private var timestamp = ""


    // Register the permission request callback
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Callback after the user grants or denies the permission
        if (isGranted) {
            // Permission is granted
            onPermissionGranted()
        } else {
            // Permission is denied
            Toast.makeText(
                this,
                "Please grant storage permission to create order",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private lateinit var lottieOverlayFragment: LottieOverlayFragment

    private val viewModel: OrderedItemsViewModel by viewModels {
        OrderedItemsVMFactory(DatabaseRepository(AppDatabase.getDatabase(this).orderDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityOrderedItemsBinding.inflate(layoutInflater)
        val root = binding.root
        setContentView(root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        customerModel = intent.getParcelableExtra(CompanyFragment.CUSTOMER_MODEL_KEY)!!
        totalBillAmount = intent.getFloatExtra(CompanyFragment.TOTAL_BILL, 0F)
        companyType = intent.getStringExtra(CompanyFragment.COMPANY_TYPE_KEY)!!
        if (intent.hasExtra("ordered_item_list")) {
            orderedItemsList = intent.getParcelableArrayListExtra("ordered_item_list")!!
        }
        if (intent.hasExtra("order_id")) {
            orderId = intent.getStringExtra("order_id")!!
        }

        orderedItemsRecyclerAdapter = OrderedItemsRecyclerAdapter(orderedItemsList)
        binding.rvOrderedItems.layoutManager = LinearLayoutManager(this)
        binding.rvOrderedItems.adapter = orderedItemsRecyclerAdapter
        binding.tvOrderedTotal.text =
            BigDecimal(totalBillAmount.toString()).setScale(2, RoundingMode.HALF_UP)
                .toFloat().toString()

        lottieOverlayFragment = LottieOverlayFragment.newInstance()

        binding.btnCompleteOrder.setOnClickListener {
            showAlertDialog(this, "Do you want to create a new order?", {
                completeOrder()
            }, {
            })
        }

        binding.btnCancelOrder.setOnClickListener {
            finish()
        }

        lifecycleScope.launch {
            viewModel.orderId.collect { value ->
                withContext(Dispatchers.Main) {
                    if (value.isNotEmpty()) {
                        Toast.makeText(
                            this@OrderedItemsActivity,
                            "Order Created Successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        lottieOverlayFragment.dismiss()
                        UtilityMethods.setBillNumber(
                            this@OrderedItemsActivity,
                            billNumber = UtilityMethods.getBillNumber(
                                this@OrderedItemsActivity,
                                companyType
                            ) + 1,
                            UtilityMethods.getBillId(this@OrderedItemsActivity, companyType),
                            companyType
                        )

                        val resultIntent = Intent()
                        resultIntent.putExtra("company", companyType)
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()

                    }
                }
            }
        }
    }

    private fun completeOrder() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            requestPermission()
        } else {
            onPermissionGranted()
        }
    }

    private fun requestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted
                onPermissionGranted()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                // Show an explanation to the user why you need the permission
                // After the user sees the explanation, try requesting the permission again
                showPermissionRationale()
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private fun onPermissionGranted() {
        lottieOverlayFragment.show(supportFragmentManager, "lottie_overlay")

        if (orderId.isEmpty()) {
            createOrderInDB()
        } else {
            updateOrder()
        }
    }

    private fun updateOrder() {
        viewModel.updateOrderObject(orderId, orderedItemsList, totalBillAmount)
    }

    private fun createOrderInDB() {
        orderId = "ORD_${UtilityMethods.getCurrentDateString("ddMMyyyy_HHmmss")}"

        val orderObject = OrdersTable(
            orderId = orderId,
            orderTotal = totalBillAmount,
            createdDate = UtilityMethods.getCurrentDateString("dd-MM-yyyy"),
            companyName = companyType,
            orderFileName = "",
            orderedItemList = orderedItemsList.toList(),
            isBackedUp = false,
            fileURI = "",
            customerModel = customerModel
        )
        viewModel.createOrderObjectInDB(orderObject)
    }

    private fun showPermissionRationale() {
        // Show a dialog or message explaining why the permission is needed
        // After explaining, request the permission again
        AlertDialog.Builder(this)
            .setTitle("Permission Needed")
            .setMessage("External storage permission is needed to store your order in csv.")
            .setPositiveButton("OK") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }
}