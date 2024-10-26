package com.sujoy.swathiagency.viewmodels.collections

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujoy.swathiagency.data.datamodels.CollectionsCustomerModel
import com.sujoy.swathiagency.network.NetworkRepository
import com.sujoy.swathiagency.utilities.DatabaseRepository
import com.sujoy.swathiagency.utilities.UtilityMethods
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CollectionsViewModel(
    private val networkRepository: NetworkRepository,
    private val databaseRepository: DatabaseRepository
) : ViewModel() {

    private val _itemData = MutableStateFlow<ArrayList<CollectionsCustomerModel>>(arrayListOf())
    val itemData: StateFlow<ArrayList<CollectionsCustomerModel>> = _itemData

    private val _totalEntryValue = MutableStateFlow(0.0F)
    val totalEntryValue: StateFlow<Float> = _totalEntryValue
    private val _totalCollectedAmount = MutableStateFlow(0.0F)
    val totalCollectedAmount: StateFlow<Float> = _totalCollectedAmount

    fun fetchCollectionsData(context: Context) {
        viewModelScope.launch {
            if(UtilityMethods.isNetworkAvailable(context)){
                val data = networkRepository.getCollectionsCSV()
                withContext(Dispatchers.Main) {
                    _itemData.value = data
                }
            }
        }
    }

    fun updateTotalEntryValue(){
        var total = 0.0
        for(item in _itemData.value){
            total += item.amountCollected + item.couponDiscount + item.displayDiscount
        }

        _totalEntryValue.value = total.toFloat()
    }

    fun updateTotalCollectedValue(){
        var total = 0.0
        for (item in _itemData.value){
            total += item.amountCollected
        }

        _totalCollectedAmount.value = total.toFloat()
    }
}