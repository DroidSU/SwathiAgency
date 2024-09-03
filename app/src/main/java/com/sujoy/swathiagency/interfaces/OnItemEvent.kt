package com.sujoy.swathiagency.interfaces

import com.sujoy.swathiagency.data.ITCItemsModel

interface OnItemEvent {
    fun onItemValueChanged(currentItem : ITCItemsModel)
}