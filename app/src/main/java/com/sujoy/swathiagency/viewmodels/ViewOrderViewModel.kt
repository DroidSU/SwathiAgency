package com.sujoy.swathiagency.viewmodels

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujoy.swathiagency.data.datamodels.CustomerOrderModel
import com.sujoy.swathiagency.data.datamodels.OrdersTable
import com.sujoy.swathiagency.utilities.Constants
import com.sujoy.swathiagency.utilities.DatabaseRepository
import com.sujoy.swathiagency.utilities.UtilityMethods
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ViewOrderViewModel(private val repository: DatabaseRepository) : ViewModel() {

    private val _ordersList = MutableStateFlow<MutableList<OrdersTable>>(
        mutableListOf()
    )
    val ordersList: StateFlow<MutableList<OrdersTable>> = _ordersList

    private val _totalOrderValue = MutableStateFlow(0F)
    val totalOrderValue: StateFlow<Float> = _totalOrderValue

    private val _orderTotalUri: MutableLiveData<Uri> = MutableLiveData()
    val orderTotalFileUri: LiveData<Uri> get() = _orderTotalUri

    private val _uploadSuccess = MutableSharedFlow<Boolean>()
    val uploadSuccess: SharedFlow<Boolean> get() = _uploadSuccess

    /*
    viewType = 0 -> All
    viewType = 1 -> ITC
    viewType = 2 -> AVT
     */
    private val _viewType: MutableStateFlow<Int> = MutableStateFlow(1)
    val viewType: StateFlow<Int> get() = _viewType

    fun getAllOrderObjects() {
        viewModelScope.launch(Dispatchers.IO) {
            val data = repository.getAllOrdersNotBackedUp()
            var total = 0F
            for (item in data) {
                total += item.orderTotal
            }
            withContext(Dispatchers.Main) {
                _ordersList.value = data.toMutableList()
                _totalOrderValue.value = total
            }
        }
    }

    fun getCompanyOrderObject(companyType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = repository.getNotBackedUpOrdersForCompany(companyType)
            var total = 0F
            for (item in data) {
                total += item.orderTotal
            }
            withContext(Dispatchers.Main) {
                _ordersList.value = data.toMutableList()
                _totalOrderValue.value = total
            }
        }
    }

    fun backupOrders(context: Context, orderList: List<OrdersTable>) {
        viewModelScope.launch(Dispatchers.IO) {
            var backupURI: Uri? = null
            for (order in orderList) {
                val file: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // App-specific external storage for Android 10 and above
                    File(context.getExternalFilesDir(null), order.fileURI)
                } else {
                    // Internal storage for devices below Android 10
                    File(context.filesDir, order.fileURI)
                }

                backupURI =
                    UtilityMethods().backupOrdersCSVFile(context, file, Uri.parse(order.fileURI))
                if (backupURI != null) {
                    setOrderIsBackedUp(order.orderId)
                } else {
                    break
                }
            }

            withContext(Dispatchers.Main) {
                _uploadSuccess.emit(backupURI != null)
            }
        }
    }

    fun backupCustomerOrderTotal(context: Context, data: List<CustomerOrderModel>, totalValue : Long) {
        viewModelScope.launch(Dispatchers.IO) {
            var companyType = ""
            companyType = if(viewType.value == 1){
                Constants.COMPANY_TYPE_ITC
            } else{
                Constants.COMPANY_TYPE_AVT
            }

            val file = UtilityMethods().createCustomerOrdersBackupCSV(context, data, companyType, totalValue)
            if(file != null && file.exists()){
                UtilityMethods().backupCustomerOrderCSV(
                    context,
                    file,
                    UtilityMethods().getFileUri(context, file)
                )
            }
        }
    }

    fun setViewType(type: Int) {
        _viewType.value = type
    }

    private fun setOrderIsBackedUp(orderId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setOrderIsBackedUp(orderId)
        }
    }
}