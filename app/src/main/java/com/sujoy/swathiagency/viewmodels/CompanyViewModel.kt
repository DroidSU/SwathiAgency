package com.sujoy.swathiagency.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujoy.swathiagency.data.datamodels.CustomerModel
import com.sujoy.swathiagency.data.datamodels.ITCItemsModel
import com.sujoy.swathiagency.network.NetworkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CompanyViewModel(private val repository: NetworkRepository) : ViewModel() {
    private val _itemData = MutableStateFlow<ArrayList<ITCItemsModel>>(arrayListOf())
    val itemData: StateFlow<ArrayList<ITCItemsModel>> = _itemData
    private val _categories = MutableStateFlow<MutableList<String>>(arrayListOf())
    val categories: StateFlow<MutableList<String>> = _categories
    private val _totalBill = MutableStateFlow(0.0F)
    val totalBill: StateFlow<Float> = _totalBill
    private val _selectedCustomerModel = MutableLiveData<CustomerModel>()
    val selectedCustomerModel: LiveData<CustomerModel> get() = _selectedCustomerModel
    private val _itemListOfSelectedCategories = MutableStateFlow<MutableList<ITCItemsModel>>(
        mutableListOf()
    )
    val itemListOfSelectedCategories: StateFlow<MutableList<ITCItemsModel>> =
        _itemListOfSelectedCategories

    private val _orderedItemsList = MutableStateFlow<MutableList<ITCItemsModel>>(mutableListOf())
    val orderedItemsList : StateFlow<MutableList<ITCItemsModel>> = _orderedItemsList

    fun fetchItemData(fileUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = repository.fetchITCItemsCSV(fileUrl)

            withContext(Dispatchers.Main) {
                _itemData.value = data
                getAllCategories()
            }
        }
    }

    private fun getAllCategories() {
        val categoryList = _itemData.value.distinctBy { it.itemGroup }.map { it.itemGroup }
        _categories.value = categoryList.toMutableList()
    }

    fun getItemsInSelectedCategory(selectedCategory: String) {
        if (selectedCategory.isNotEmpty()) {
            _itemListOfSelectedCategories.value =
                _itemData.value.filter { it.itemGroup == selectedCategory }.toMutableList()
        } else {
            _itemListOfSelectedCategories.value = _itemData.value.toMutableList()
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

    fun addOrderItem(currentItem : ITCItemsModel){
        _orderedItemsList.value.removeIf { it.itemName == currentItem.itemName && (currentItem.numberOfBoxesOrdered > 0 || currentItem.numberOfPcsOrdered > 0) }

        if (currentItem.numberOfBoxesOrdered > 0 || currentItem.numberOfPcsOrdered > 0) {
            _orderedItemsList.value.add(currentItem)
        }

        updateTotalBillValue()
    }
}