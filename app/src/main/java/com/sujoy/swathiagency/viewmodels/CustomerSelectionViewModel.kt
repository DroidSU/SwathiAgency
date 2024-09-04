package com.sujoy.swathiagency.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujoy.swathiagency.network.NetworkRepository
import com.sujoy.swathiagency.data.datamodels.CustomerModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CustomerSelectionViewModel(private val repository: NetworkRepository) : ViewModel() {

    private val _csvData = MutableStateFlow<ArrayList<CustomerModel>>(arrayListOf())
    val csvData: StateFlow<ArrayList<CustomerModel>> = _csvData

    fun loadCsvData(fileUrl: String) {
        viewModelScope.launch {
            val data = repository.fetchCustomerCSV(fileUrl)
            _csvData.value = data
        }
    }
}