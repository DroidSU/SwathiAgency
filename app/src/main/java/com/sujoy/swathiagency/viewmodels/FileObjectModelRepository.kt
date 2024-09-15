package com.sujoy.swathiagency.viewmodels

import com.sujoy.swathiagency.data.dbModels.CustomerOrderModel
import com.sujoy.swathiagency.data.dbModels.OrderFileModel
import com.sujoy.swathiagency.database.OrderDao

class FileObjectModelRepository(private val orderDao: OrderDao) {

    suspend fun insert(fileObjectModels: OrderFileModel) {
        if (orderDao.isDatePresent(fileObjectModels.createdOn, fileObjectModels.companyName) == 0) {
            orderDao.createOrderFile(fileObjectModels)
        }
    }

    suspend fun getFilesNotBackedUp(companyType : String): List<OrderFileModel> {
        return orderDao.getFilesNotBackedUp(companyType)
    }

    suspend fun markAsBackedUp(fileName: String, companyType: String) {
        return orderDao.setIsBackedUp(fileName, companyType)
    }

    suspend fun getCustomerOrderObjects(orderId : String) : CustomerOrderModel? {
        return orderDao.getCustomerOrderObject(orderId)
    }

    suspend fun insertOrUpdateCustomerOrder(companyOrders: CustomerOrderModel) {
        orderDao.createCustomerOrderObject(companyOrderModel = companyOrders)
    }

    suspend fun getAllOrdersForDate(date : String) : List<CustomerOrderModel> {
        return orderDao.getAllOrdersForDate(date)
    }
}