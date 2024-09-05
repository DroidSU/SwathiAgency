package com.sujoy.swathiagency.ui

import android.Manifest
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.sujoy.swathiagency.R
import com.sujoy.swathiagency.adapters.OrderedItemsRecyclerAdapter
import com.sujoy.swathiagency.data.datamodels.CustomerModel
import com.sujoy.swathiagency.data.datamodels.ITCItemsModel
import com.sujoy.swathiagency.data.dbModels.FileObjectModels
import com.sujoy.swathiagency.database.AppDatabase
import com.sujoy.swathiagency.databinding.ActivityOrderedItemsBinding
import com.sujoy.swathiagency.utilities.Constants
import com.sujoy.swathiagency.utilities.UtilityMethods
import com.sujoy.swathiagency.utilities.UtilityMethods.Companion.showAlertDialog
import com.sujoy.swathiagency.viewmodels.FileObjectModelRepository
import com.sujoy.swathiagency.viewmodels.OrderedItemsVMFactory
import com.sujoy.swathiagency.viewmodels.OrderedItemsViewModel
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode

class OrderedItemsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderedItemsBinding
    private var orderedItemsList: ArrayList<ITCItemsModel> = arrayListOf()
    private lateinit var customerModel: CustomerModel
    private var totalBillAmount: Float = 0F
    private lateinit var orderedItemsRecyclerAdapter: OrderedItemsRecyclerAdapter
    private var companyType = Constants.COMPANY_TYPE_ITC


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
        OrderedItemsVMFactory(FileObjectModelRepository(AppDatabase.getDatabase(this).orderDao()))
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

        orderedItemsList = intent.getParcelableArrayListExtra("ordered_item_list")!!
        customerModel = intent.getParcelableExtra(CompanyFragment.CUSTOMER_MODEL_KEY)!!
        totalBillAmount = intent.getFloatExtra(CompanyFragment.TOTAL_BILL, 0F)
        companyType = intent.getStringExtra(CompanyFragment.COMPANY_TYPE_KEY)!!

        orderedItemsRecyclerAdapter = OrderedItemsRecyclerAdapter(orderedItemsList)
        binding.rvOrderedItems.layoutManager = LinearLayoutManager(this)
        binding.rvOrderedItems.adapter = orderedItemsRecyclerAdapter
        binding.tvOrderedTotal.text = BigDecimal(totalBillAmount.toString()).setScale(2, RoundingMode.HALF_UP)
            .toFloat().toString()

        lottieOverlayFragment = LottieOverlayFragment.newInstance()

        binding.btnCompleteOrder.setOnClickListener {
            showAlertDialog(this, "Do you want to create a new order?", {
                completeOrder()
            }, {
            })
        }

        binding.btnCancelOrder.setOnClickListener {
            startActivity(Intent(this, ViewItemsActivity::class.java))
            finish()
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

        val csvFile = UtilityMethods().createOrUpdateCsvFile(
            this,
            orderedItemsList,
            customerModel,
            companyType
        )

        if (csvFile!!.exists()) {
            saveOrderInDB(csvFile)

            Toast.makeText(
                this,
                "Order Created Successfully!",
                Toast.LENGTH_SHORT
            ).show()
            lottieOverlayFragment.dismiss()
            UtilityMethods.setBillNumber(
                this,
                billNumber = UtilityMethods.getBillNumber(this) + 1,
                UtilityMethods.getBillId(this)
            )
            startActivity(
                Intent(
                    this,
                    CustomerSelectionActivity::class.java
                )
            )
            finish()
        }
    }

    private fun saveOrderInDB(csvFile: File) {
        val orderFileDBObject = FileObjectModels(
            fileName = csvFile.nameWithoutExtension,
            createdBy = customerModel.customerName,
            createdOn = "",
            fileURI = csvFile.absolutePath,
            companyName = companyType
        )

        viewModel.createOrderFileInDB(this, orderFileDBObject)
    }

    private fun showPermissionRationale() {
        // Show a dialog or message explaining why the permission is needed
        // After explaining, request the permission again
        AlertDialog.Builder(this)
            .setTitle("Permission Needed")
            .setMessage("This app needs the permission to do something.")
            .setPositiveButton("OK") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }
}