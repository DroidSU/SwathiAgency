package com.sujoy.swathiagency.viewmodels

import com.sujoy.swathiagency.data.datamodels.CustomerModel
import com.sujoy.swathiagency.data.datamodels.CustomerOrderModel
import com.sujoy.swathiagency.data.datamodels.ItemsModel
import com.sujoy.swathiagency.data.datamodels.OrderFileModel
import com.sujoy.swathiagency.database.OrderDao

class DatabaseRepository(private val orderDao: OrderDao) {

    suspend fun insert(fileObjectModels: OrderFileModel) {
//        if (orderDao.isExistingFile(fileObjectModels.createdOn, fileObjectModels.companyName, fileObjectModels.customerName) == 0) {
//            orderDao.createOrderFile(fileObjectModels)
//        }

        orderDao.createOrderFile(fileObjectModels)
    }

    suspend fun getFilesNotBackedUp(companyType: String): List<OrderFileModel> {
        return orderDao.getFilesNotBackedUp(companyType)
    }

    suspend fun markAsBackedUp(orderId: String) {
        return orderDao.setIsBackedUp(orderId)
    }

    suspend fun getCustomerOrderObjects(orderId: String): CustomerOrderModel? {
        return orderDao.getCustomerOrderObject(orderId)
    }

    suspend fun insertOrUpdateCustomerOrder(companyOrders: CustomerOrderModel) {
        orderDao.createCustomerOrderObject(companyOrderModel = companyOrders)
    }

    suspend fun getAllOrdersForDate(date: String): List<CustomerOrderModel> {
        return orderDao.getAllOrdersForDate(date)
    }

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

}