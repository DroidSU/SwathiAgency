package com.sujoy.swathiagency.utilities

import com.sujoy.swathiagency.data.datamodels.CustomerModel
import com.sujoy.swathiagency.data.datamodels.ItemsModel
import com.sujoy.swathiagency.data.datamodels.OrdersTable
import com.sujoy.swathiagency.database.OrderDao

class DatabaseRepository(private val orderDao: OrderDao) {
    suspend fun getAllCustomers(): ArrayList<CustomerModel> {
        return ArrayList(orderDao.getAllCustomers())
    }

    suspend fun saveCustomerDataInDB(customerData: List<CustomerModel>) {
        return orderDao.createCustomerObject(customerData)
    }

    suspend fun getAllItemsFromCompany(companyType: String): ArrayList<ItemsModel> {
        return ArrayList(orderDao.getAllItems(companyType))
    }

    suspend fun addItems(items: List<ItemsModel>) {
        return orderDao.createItemsObjects(items)
    }

    suspend fun createNewOrder(orderObject : OrdersTable) {
        return orderDao.createOrderObject(orderObject)
    }

    suspend fun getAllOrdersNotBackedUp(): List<OrdersTable> {
        return orderDao.getAllOrdersNotBackedUp()
    }

    suspend fun getNotBackedUpOrdersForCompany(companyType: String) : List<OrdersTable> {
        return orderDao.getCompanyOrdersNotBackedUp(companyType)
    }

    suspend fun setOrderIsBackedUp(orderId : String) {
        return orderDao.setIsBackedUp(orderId)
    }
}