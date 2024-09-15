package com.sujoy.swathiagency.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sujoy.swathiagency.data.dbModels.CustomerOrderModel
import com.sujoy.swathiagency.data.dbModels.OrderFileModel
import com.sujoy.swathiagency.utilities.Constants

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createOrderFile(orderModel: OrderFileModel)

    @Query("SELECT * FROM ${Constants.TABLE_FILE_OBJECTS} where ${Constants.IS_BACKED_UP} = 0 AND ${Constants.COMPANY_NAME} = :companyType")
    suspend fun getFilesNotBackedUp(companyType: String) : List<OrderFileModel>

    @Query("UPDATE ${Constants.TABLE_FILE_OBJECTS} SET ${Constants.IS_BACKED_UP} = 1 where ${Constants.FILE_NAME} = :fileName AND ${Constants.COMPANY_NAME} = :companyType")
    suspend fun setIsBackedUp(fileName : String, companyType: String)

    @Query("SELECT EXISTS(SELECT 1 FROM ${Constants.TABLE_FILE_OBJECTS} WHERE ${Constants.CREATED_ON_DATE} = :dateString AND ${Constants.COMPANY_NAME} = :companyType)")
    suspend fun isDatePresent(dateString : String, companyType : String) : Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createCustomerOrderObject(companyOrderModel: CustomerOrderModel)

    @Query("SELECT * FROM ${Constants.TABLE_ORDERS} where ${Constants.ORDER_ID} = :orderId LIMIT 1")
    suspend fun getCustomerOrderObject(orderId : String) : CustomerOrderModel?

    @Query("SELECT * FROM ${Constants.TABLE_ORDERS} where ${Constants.DATE} = :date")
    suspend fun getAllOrdersForDate(date : String) : List<CustomerOrderModel>

    @Query("SELECT * FROM ${Constants.TABLE_ORDERS} where ${Constants.CUSTOMER_NAME} = :customerName LIMIT 1")
    suspend fun getRecentOrder(customerName : String) : CustomerOrderModel
}