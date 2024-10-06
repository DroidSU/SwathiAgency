package com.sujoy.swathiagency.interfaces

import com.sujoy.swathiagency.data.datamodels.CustomerModel
import com.sujoy.swathiagency.data.datamodels.ItemsModel

interface OnCustomerRecyclerItemClick {
    fun onItemClicked(customer : CustomerModel)
}

interface OnViewOrderRecyclerItemClick{
    fun onItemClicked(orderId : String, customerModel : CustomerModel, itemsModel: MutableList<ItemsModel>)
}

