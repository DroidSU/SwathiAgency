package com.sujoy.swathiagency.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujoy.swathiagency.data.dbModels.FileObjectModels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OrderedItemsViewModel(private val fileObjectsRepository: FileObjectModelRepository) :
    ViewModel() {

    fun createOrderFileInDB(context: Context, fileObjectModels: FileObjectModels) {
        viewModelScope.launch(Dispatchers.IO) {
            fileObjectsRepository.insert(fileObjectModels)
        }
    }
}