package com.sujoy.swathiagency.viewmodels

import com.sujoy.swathiagency.data.dbModels.FileObjectModels
import com.sujoy.swathiagency.database.OrderDao

class FileObjectModelRepository(private val orderDao: OrderDao) {

    suspend fun insert(fileObjectModels: FileObjectModels) {
        if (orderDao.isDatePresent(fileObjectModels.createdOn, fileObjectModels.companyName) == 0) {
            orderDao.createOrder(fileObjectModels)
        }
    }

    suspend fun getFilesNotBackedUp(companyType : String): List<FileObjectModels> {
        return orderDao.getOrdersNotBackedUp(companyType)
    }

    suspend fun markAsBackedUp(companyType: String) {
        return orderDao.updateIsBackedUp(companyType)
    }
}