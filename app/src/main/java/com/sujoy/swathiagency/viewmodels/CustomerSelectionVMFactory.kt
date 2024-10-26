package com.sujoy.swathiagency.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sujoy.swathiagency.network.NetworkRepository
import com.sujoy.swathiagency.utilities.DatabaseRepository

class CustomerSelectionVMFactory(private val repository: NetworkRepository, private val databaseRepository: DatabaseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustomerSelectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CustomerSelectionViewModel(repository, databaseRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}