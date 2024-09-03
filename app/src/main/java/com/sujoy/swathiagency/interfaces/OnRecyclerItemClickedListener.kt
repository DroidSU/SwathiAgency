package com.sujoy.swathiagency.interfaces

import com.sujoy.swathiagency.data.CustomerModel

interface OnRecyclerItemClickedListener {
    fun onCustomerClicked(customer : CustomerModel)
}