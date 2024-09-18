package com.sujoy.swathiagency.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujoy.swathiagency.data.datamodels.CustomerModel
import com.sujoy.swathiagency.network.NetworkRepository
import com.sujoy.swathiagency.utilities.DatabaseRepository
import com.sujoy.swathiagency.utilities.UtilityMethods
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CustomerSelectionViewModel(private val repository: NetworkRepository, private val databaseRepository : DatabaseRepository) : ViewModel() {

    private val _csvData = MutableStateFlow<ArrayList<CustomerModel>>(arrayListOf())
    val csvData: StateFlow<ArrayList<CustomerModel>> = _csvData

    fun getCustomerData(context : Context) {
        viewModelScope.launch {
            if(UtilityMethods.isNetworkAvailable(context)){
                val data = repository.getCustomerCSV()
                databaseRepository.saveCustomerDataInDB(data)
                _csvData.value = data
            }
            else{
                val data = databaseRepository.getAllCustomers()
                _csvData.value = data
            }
        }
    }
}