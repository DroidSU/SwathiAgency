package com.sujoy.swathiagency.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sujoy.swathiagency.network.NetworkRepository

class ITCVMFactory(private val repository: NetworkRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ITCViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ITCViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}