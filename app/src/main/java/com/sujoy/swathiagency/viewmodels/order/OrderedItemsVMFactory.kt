package com.sujoy.swathiagency.viewmodels.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sujoy.swathiagency.utilities.DatabaseRepository

class OrderedItemsVMFactory(private val repository: DatabaseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderedItemsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrderedItemsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}