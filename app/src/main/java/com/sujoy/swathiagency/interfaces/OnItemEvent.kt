package com.sujoy.swathiagency.interfaces

import com.sujoy.swathiagency.data.datamodels.ITCItemsModel

interface OnItemEvent {
    fun onItemValueChanged(currentItem : ITCItemsModel)
}