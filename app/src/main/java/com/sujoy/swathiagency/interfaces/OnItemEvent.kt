package com.sujoy.swathiagency.interfaces

import com.sujoy.swathiagency.data.datamodels.ItemsModel

interface OnItemEvent {
    fun onItemValueChanged(currentItem : ItemsModel)
}