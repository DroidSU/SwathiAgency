package com.sujoy.swathiagency.viewmodels.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sujoy.swathiagency.network.NetworkRepository
import com.sujoy.swathiagency.utilities.DatabaseRepository

class CollectionsVMFactory(private val repository: NetworkRepository, private val databaseRepository: DatabaseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CollectionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CollectionsViewModel(repository, databaseRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}