package com.sujoy.swathiagency.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujoy.swathiagency.data.datamodels.CustomerOrderModel
import com.sujoy.swathiagency.data.datamodels.OrderFileModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OrderedItemsViewModel(private val fileObjectsRepository: DatabaseRepository) :
    ViewModel() {

    fun createOrderFileInDB(fileObjectModels: OrderFileModel) {
        viewModelScope.launch(Dispatchers.IO) {
            fileObjectsRepository.insert(fileObjectModels)
        }
    }

    fun createCompanyOrderObject(companyOrderModel: CustomerOrderModel) {
        viewModelScope.launch (Dispatchers.IO) {
            fileObjectsRepository.insertOrUpdateCustomerOrder(companyOrderModel)
        }
    }

    fun getCompanyOrderObject(orderId : String) : CustomerOrderModel? {
        var companyOrderModel : CustomerOrderModel? = null
        viewModelScope.launch (Dispatchers.IO) {
            companyOrderModel = fileObjectsRepository.getCustomerOrderObjects(orderId)
        }
        return companyOrderModel
    }
}