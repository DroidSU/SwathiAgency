package com.sujoy.swathiagency.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujoy.swathiagency.data.datamodels.CustomerModel
import com.sujoy.swathiagency.data.datamodels.ItemsModel
import com.sujoy.swathiagency.network.NetworkRepository
import com.sujoy.swathiagency.utilities.DatabaseRepository
import com.sujoy.swathiagency.utilities.UtilityMethods
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CompanyViewModel(
    private val repository: NetworkRepository,
    private val databaseRepository: DatabaseRepository
) : ViewModel() {
    private val _itemData = MutableStateFlow<ArrayList<ItemsModel>>(arrayListOf())
    val itemData: StateFlow<ArrayList<ItemsModel>> = _itemData
    private val _categories = MutableStateFlow<MutableList<String>>(arrayListOf())
    val categories: StateFlow<MutableList<String>> = _categories
    private val _totalBill = MutableStateFlow(0.0F)
    val totalBill: StateFlow<Float> = _totalBill
    private val _selectedCustomerModel = MutableLiveData<CustomerModel>()
    val selectedCustomerModel: LiveData<CustomerModel> get() = _selectedCustomerModel
    private val _itemListOfSelectedCategories = MutableStateFlow<MutableList<ItemsModel>>(
        mutableListOf()
    )
    val itemListOfSelectedCategories: StateFlow<MutableList<ItemsModel>> =
        _itemListOfSelectedCategories

    private val _orderedItemsList = MutableStateFlow<MutableList<ItemsModel>>(mutableListOf())
    val orderedItemsList: StateFlow<MutableList<ItemsModel>> = _orderedItemsList

    fun fetchItemData(context: Context, fileUrl: String, companyType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (UtilityMethods.isNetworkAvailable(context)) {
                val data = repository.getItemsCSV(companyType)
                databaseRepository.addItems(data.toList())
                withContext(Dispatchers.Main) {
                    _itemData.value = data
                    getAllCategories()
                }
            } else {
                val data = databaseRepository.getAllItemsFromCompany(companyType)
                withContext(Dispatchers.Main) {
                    _itemData.value = data
                    getAllCategories()
                }
            }
        }
    }

    private fun getAllCategories() {
        val categoryList = _itemData.value.distinctBy { it.itemGroup }.map { it.itemGroup }
        _categories.value = categoryList.toMutableList()
    }

    fun getItemsInSelectedCategory(selectedCategory: String) {
        if (selectedCategory.isNotEmpty()) {
            // Check if the category data is already cached

            _itemListOfSelectedCategories.value =
                _itemData.value.filter { it.itemGroup == selectedCategory }
                    .sortedBy { it.taxablePcsRate.toFloat() }.toMutableList()
        } else {
            _itemListOfSelectedCategories.value =
                _itemData.value.sortedBy { it.taxablePcsRate.toFloat() }.toMutableList()
        }
    }

    private fun updateTotalBillValue() {
        var total = 0f;
        _orderedItemsList.value.forEachIndexed { _, itcItemsModel ->
            total += itcItemsModel.totalAmount
        }

        _totalBill.value = total
    }

    fun setCustomerModel(selectedCustomerModel: CustomerModel) {
        _selectedCustomerModel.value = selectedCustomerModel
    }

    fun addOrderItem(currentItem: ItemsModel) {
        if (currentItem.selected) {
            if (_orderedItemsList.value.any{ it.itemID == currentItem.itemID}) {
                val currentItemIndex = _orderedItemsList.value.indexOfFirst { it.itemID == currentItem.itemID }
                if (currentItemIndex != -1) {
                    _orderedItemsList.value[currentItemIndex] = currentItem
                }
            } else {
                _orderedItemsList.value.add(currentItem)
            }

            updateTotalBillValue()
        }
    }
}