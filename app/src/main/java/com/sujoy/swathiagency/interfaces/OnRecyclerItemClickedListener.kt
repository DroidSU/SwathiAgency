package com.sujoy.swathiagency.interfaces

import com.sujoy.swathiagency.data.datamodels.CustomerModel

interface OnRecyclerItemClickedListener {
    fun onCustomerClicked(customer : CustomerModel)
}