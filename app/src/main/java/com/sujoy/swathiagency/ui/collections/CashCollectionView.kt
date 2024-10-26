package com.sujoy.swathiagency.ui.collections

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sujoy.swathiagency.R
import com.sujoy.swathiagency.adapters.CollectionsCustomersRecyclerAdapter
import com.sujoy.swathiagency.data.datamodels.CollectionsCustomerModel
import com.sujoy.swathiagency.database.AppDatabase
import com.sujoy.swathiagency.databinding.ActivityCashCollectionViewBinding
import com.sujoy.swathiagency.interfaces.OnCashCollectionRecyclerItemClick
import com.sujoy.swathiagency.interfaces.OnCashEditDialogDismissed
import com.sujoy.swathiagency.interfaces.OnCashEditSubmitTapped
import com.sujoy.swathiagency.network.NetworkRepository
import com.sujoy.swathiagency.utilities.DatabaseRepository
import com.sujoy.swathiagency.viewmodels.collections.CollectionsVMFactory
import com.sujoy.swathiagency.viewmodels.collections.CollectionsViewModel
import kotlinx.coroutines.launch

class CashCollectionView : AppCompatActivity(), OnCashCollectionRecyclerItemClick, OnCashEditDialogDismissed, OnCashEditSubmitTapped {
    private lateinit var binding: ActivityCashCollectionViewBinding
    private lateinit var adapter: CollectionsCustomersRecyclerAdapter

    private var collectionsCustomerList = mutableListOf<CollectionsCustomerModel>()
    private lateinit var editDialog : DialogFragment

    private val viewModel: CollectionsViewModel by viewModels {
        CollectionsVMFactory(
            NetworkRepository(this),
            DatabaseRepository(AppDatabase.getDatabase(this).orderDao())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCashCollectionViewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        adapter = CollectionsCustomersRecyclerAdapter(this, collectionsCustomerList, this)
        binding.rvCollectionsCustomers.layoutManager = LinearLayoutManager(this)
        binding.rvCollectionsCustomers.adapter = adapter

        lifecycleScope.launch {
            viewModel.itemData.collect { data ->
                if (data.isNotEmpty()) {
                    adapter.updateData(data)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.totalEntryValue.collect{
                binding.tvTotalEntryValue.text = it.toString()
            }
        }

        lifecycleScope.launch {
            viewModel.totalCollectedAmount.collect{
                binding.tvTotalCollectedAmount.text = it.toString()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchCollectionsData(this)
    }

    override fun onItemClicked(cashCollectionsCustomerModel: CollectionsCustomerModel) {
        editDialog = CashCollectionDialog(cashCollectionsCustomerModel, this, this)
        editDialog.show(supportFragmentManager, "CashCollectionDialog")
        editDialog.isCancelable = false
    }

    override fun onCashEditDialogDismissed() {
        editDialog.dismiss()
    }

    override fun onCashEditSubmitTapped(cashCollectionModel: CollectionsCustomerModel) {
        adapter.updateItem(cashCollectionModel)
        editDialog.dismiss()

        viewModel.updateTotalEntryValue()
        viewModel.updateTotalCollectedValue()
    }
}