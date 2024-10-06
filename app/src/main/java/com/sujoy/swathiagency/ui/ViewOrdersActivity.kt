package com.sujoy.swathiagency.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.sujoy.swathiagency.R
import com.sujoy.swathiagency.adapters.CustomerOrderRecyclerAdapter
import com.sujoy.swathiagency.data.datamodels.CustomerModel
import com.sujoy.swathiagency.data.datamodels.CustomerOrderModel
import com.sujoy.swathiagency.data.datamodels.ItemsModel
import com.sujoy.swathiagency.data.datamodels.OrdersTable
import com.sujoy.swathiagency.database.AppDatabase
import com.sujoy.swathiagency.databinding.ActivityViewOrdersBinding
import com.sujoy.swathiagency.interfaces.OnViewOrderRecyclerItemClick
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

class ViewOrdersActivity : AppCompatActivity(), OnViewOrderRecyclerItemClick {
    private lateinit var binding: ActivityViewOrdersBinding

    private var ordersArrayList: MutableList<OrdersTable> = mutableListOf()
    private lateinit var adapter: CustomerOrderRecyclerAdapter
    private lateinit var lottieOverlayFragment: LottieOverlayFragment
    private var viewType = MutableLiveData(1)

    private lateinit var dragHelper: ItemTouchHelper
    private lateinit var swipeHelper: ItemTouchHelper


    private val viewModel: ViewOrderViewModel by viewModels {
        ViewOrderVMFactory(DatabaseRepository(AppDatabase.getDatabase(this).orderDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityViewOrdersBinding.inflate(layoutInflater)
        val root = binding.root
        setContentView(root)

        val deleteIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_delete_24, null)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lottieOverlayFragment = LottieOverlayFragment.newInstance()

        binding.rvOrders.layoutManager = LinearLayoutManager(this)
//        binding.rvOrders.addItemDecoration(SpacesItemDecoration(16))
        adapter = CustomerOrderRecyclerAdapter(this, ordersArrayList, this)
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

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.viewType.collect { data ->
                withContext(Dispatchers.Main) {
                    viewType.value = data
                    updateDataOnUI()
                }
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

        swipeHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = true

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                viewModel.removeOrderById(ordersArrayList[pos].orderId)
                adapter.notifyItemRemoved(pos)
                Snackbar.make(
                    findViewById(binding.root.id),
                    "Deleted",
                    Snackbar.LENGTH_SHORT
                ).show()
            }

//            override fun onChildDraw(
//                canvas: Canvas,
//                recyclerView: RecyclerView,
//                viewHolder: RecyclerView.ViewHolder,
//                dX: Float,
//                dY: Float,
//                actionState: Int,
//                isCurrentlyActive: Boolean
//            ) {
//                //1. Background color based upon direction swiped
//                canvas.drawColor(Color.GRAY)
//
//                //2. Printing the icons
//                val textMargin = resources.getDimension(R.dimen.text_margin)
//                    .roundToInt()
//                deleteIcon.bounds = Rect(
//                    textMargin,
//                    viewHolder.itemView.top + textMargin + 8.dp,
//                    textMargin + deleteIcon.intrinsicWidth,
//                    viewHolder.itemView.top + deleteIcon.intrinsicHeight
//                            + textMargin + 8.dp
//                )
//
//                deleteIcon.draw(canvas)
//
//                archiveIcon.bounds = Rect(
//                    width - textMargin - archiveIcon.intrinsicWidth,
//                    viewHolder.itemView.top + textMargin + 8.dp,
//                    width - textMargin,
//                    viewHolder.itemView.top + archiveIcon.intrinsicHeight
//                            + textMargin + 8.dp
//                )
//
//                //3. Drawing icon based upon direction swiped
//                if (dX > 0)  else archiveIcon.draw(canvas)
//
//                super.onChildDraw(
//                    canvas,
//                    recyclerView,
//                    viewHolder,
//                    dX,
//                    dY,
//                    actionState,
//                    isCurrentlyActive
//                )
//            }
        })

        swipeHelper.attachToRecyclerView(binding.rvOrders)
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

//                R.id.backup_all -> {
////                    backupOrders("")
////                    backupTotalOrder()
//                    true
//                }

                else -> false
            }
        }

        popupMenu.show()
        updateDataOnUI()
    }

    private fun updateDataOnUI() {
        when (viewType.value) {
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

    private fun backupOrders(companyType: String) {
        if (UtilityMethods.isNetworkAvailable(this)) {
            lottieOverlayFragment.show(supportFragmentManager, "lottie_overlay")

            lifecycleScope.launch(Dispatchers.IO) {
                val ordersGroupedByDate = ordersArrayList.groupBy { it.createdDate }

                for ((date, itemList) in ordersGroupedByDate) {
                    for (item in itemList) {
                        val csvFile = UtilityMethods().createOrUpdateCsvFile(
                            this@ViewOrdersActivity,
                            item.orderedItemList,
                            item.customerModel,
                            companyType,
                            UtilityMethods.getCurrentDateString("ddMMyyyy")
                        )

                        if (csvFile != null) {
                            viewModel.updateOrderFileURI(
                                orderId = item.orderId,
                                csvFile.nameWithoutExtension,
                                UtilityMethods().getFileUri(this@ViewOrdersActivity, csvFile)
                                    .toString()
                            )
                        }
                    }
                }

                viewModel.backupOrders(
                    this@ViewOrdersActivity,
                    ordersArrayList.filter { it.companyName == companyType }.toList()
                )
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
            if (viewType.value == 1) {
                orderList = orderList.filter { it.companyName == Constants.COMPANY_TYPE_ITC }
            } else if (viewType.value == 2) {
                orderList = orderList.filter { it.companyName == Constants.COMPANY_TYPE_AVT }
            }

            for (order in orderList) {
                customerOrderTotalList.add(
                    CustomerOrderModel(
                        order.orderId,
                        order.customerModel.customerName,
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

    override fun onItemClicked(
        orderId: String,
        customerModel: CustomerModel,
        itemsModel: MutableList<ItemsModel>
    ) {
        startActivity(
            Intent(
                this,
                ViewItemsActivity::class.java
            ).putExtra("customer_model", customerModel).putParcelableArrayListExtra(
                "order_list",
                ArrayList(itemsModel)
            ).putExtra("order_id", orderId).putExtra("index", viewType.value)
        )

        finish()
    }
}