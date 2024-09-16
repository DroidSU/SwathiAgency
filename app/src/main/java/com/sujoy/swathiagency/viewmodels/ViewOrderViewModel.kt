package com.sujoy.swathiagency.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujoy.swathiagency.data.datamodels.CustomerOrderModel
import com.sujoy.swathiagency.utilities.UtilityMethods
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewOrderViewModel(private val repository: DatabaseRepository) : ViewModel() {

    private val _customerOrdersList = MutableStateFlow<MutableList<CustomerOrderModel>>(
        mutableListOf()
    )
    val customerOrderList: StateFlow<MutableList<CustomerOrderModel>> = _customerOrdersList

    private val _totalOrderValue = MutableStateFlow(0F)
    val totalOrderValue: StateFlow<Float> = _totalOrderValue

    private val _uri: MutableLiveData<Uri> = MutableLiveData()
    val uri: LiveData<Uri> get() = _uri

    fun getAllOrderObjects(date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = repository.getAllOrdersForDate(date)
            var total = 0F
            for (item in data) {
                total += item.orderTotal
            }
            withContext(Dispatchers.Main) {
                _customerOrdersList.value = data.toMutableList()
                _totalOrderValue.value = total
            }
        }
    }

    fun backupCustomerOrderTotal(context: Context, data: List<CustomerOrderModel>){
        viewModelScope.launch(Dispatchers.IO) {
            val backupFile = UtilityMethods().createCustomerOrdersBackupCSV(context, data, )
            backupFile?.let {
                withContext(Dispatchers.Main) {
                    _uri.value = UtilityMethods().backupCustomerOrderCSV(context, it)
                }
            }
        }
    }
}