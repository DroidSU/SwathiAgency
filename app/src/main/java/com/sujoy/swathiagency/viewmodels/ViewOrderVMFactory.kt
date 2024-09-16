package com.sujoy.swathiagency.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewOrderVMFactory(private val repository: DatabaseRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViewOrderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ViewOrderViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}