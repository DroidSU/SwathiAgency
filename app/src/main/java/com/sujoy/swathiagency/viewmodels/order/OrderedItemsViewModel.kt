package com.sujoy.swathiagency.viewmodels.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujoy.swathiagency.data.datamodels.ItemsModel
import com.sujoy.swathiagency.data.datamodels.OrdersTable
import com.sujoy.swathiagency.utilities.DatabaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrderedItemsViewModel(private val databaseRepository: DatabaseRepository) :
    ViewModel() {

        private val _orderId = MutableStateFlow("")
    val orderId : StateFlow<String> = _orderId

    fun createOrderObjectInDB(orderObject : OrdersTable){
        viewModelScope.launch (Dispatchers.IO) {
            databaseRepository.createNewOrder(orderObject)
            _orderId.value = orderObject.orderId
        }
    }

    fun updateOrderObject(orderId : String, orderedItemsList : List<ItemsModel>, orderTotal : Float) {
        viewModelScope.launch (Dispatchers.IO) {
            databaseRepository.updateOrder(orderId, orderedItemsList, orderTotal)
            _orderId.value = orderId
        }
    }
}