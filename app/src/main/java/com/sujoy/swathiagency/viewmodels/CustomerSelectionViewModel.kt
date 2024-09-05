package com.sujoy.swathiagency.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujoy.swathiagency.data.datamodels.CustomerModel
import com.sujoy.swathiagency.data.dbModels.FileObjectModels
import com.sujoy.swathiagency.network.NetworkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CustomerSelectionViewModel(private val repository: NetworkRepository, private val databaseRepository : FileObjectModelRepository) : ViewModel() {

    private val _csvData = MutableStateFlow<ArrayList<CustomerModel>>(arrayListOf())
    val csvData: StateFlow<ArrayList<CustomerModel>> = _csvData
    private val _fileList = MutableStateFlow<List<FileObjectModels>>(listOf())
    val fileList : StateFlow<List<FileObjectModels>> = _fileList

    fun loadCsvData(fileUrl: String) {
        viewModelScope.launch {
            val data = repository.fetchCustomerCSV(fileUrl)
            _csvData.value = data
        }
    }

    fun getFilesNotBackedUp(companyType : String) {
        viewModelScope.launch {
            val data = databaseRepository.getFilesNotBackedUp(companyType)
            _fileList.value = data
        }
    }

    fun markFilesAsBackedUp(companyType: String) {
        viewModelScope.launch {
            databaseRepository.markAsBackedUp(companyType)
        }
    }
}