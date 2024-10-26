package com.sujoy.swathiagency.interfaces

import com.sujoy.swathiagency.data.datamodels.CollectionsCustomerModel

interface OnCashEditDialogDismissed {
    fun onCashEditDialogDismissed()
}

interface OnCashEditSubmitTapped {
    fun onCashEditSubmitTapped(cashCollectionModel: CollectionsCustomerModel)
}