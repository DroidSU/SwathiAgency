package com.sujoy.swathiagency.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sujoy.swathiagency.data.datamodels.CustomerModel
import com.sujoy.swathiagency.data.datamodels.CustomerOrderModel
import com.sujoy.swathiagency.data.datamodels.ItemsModel
import com.sujoy.swathiagency.data.datamodels.OrderFileModel
import com.sujoy.swathiagency.utilities.Constants

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createOrderFile(orderModel: OrderFileModel)

    @Query("SELECT * FROM ${Constants.TABLE_FILE_OBJECTS} where ${Constants.COMPANY_NAME} = :companyType AND ${Constants.IS_BACKED_UP} = 0")
    suspend fun getFilesNotBackedUp(companyType: String): List<OrderFileModel>

    @Query("UPDATE ${Constants.TABLE_FILE_OBJECTS} SET ${Constants.IS_BACKED_UP} = 1 where ${Constants.ORDER_ID} = :orderId")
    suspend fun setIsBackedUp(orderId: String)

//    @Query("SELECT EXISTS(SELECT 1 FROM ${Constants.TABLE_FILE_OBJECTS} WHERE ${Constants.CREATED_ON_DATE} = :dateString AND ${Constants.COMPANY_NAME} = :companyType)")
//    suspend fun isExistingFile(dateString: String, companyType: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createCustomerOrderObject(companyOrderModel: CustomerOrderModel)

    @Query("SELECT * FROM ${Constants.TABLE_ORDERS} where ${Constants.ORDER_ID} = :orderId LIMIT 1")
    suspend fun getCustomerOrderObject(orderId: String): CustomerOrderModel?

    @Query("SELECT * FROM ${Constants.TABLE_ORDERS} where ${Constants.DATE} = :date")
    suspend fun getAllOrdersForDate(date: String): List<CustomerOrderModel>

    @Query("SELECT * FROM ${Constants.TABLE_ORDERS} where ${Constants.CUSTOMER_NAME} = :customerName LIMIT 1")
    suspend fun getRecentOrder(customerName: String): CustomerOrderModel

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createCustomerObject(customerModel: List<CustomerModel>)

    @Query("SELECT * FROM ${Constants.TABLE_CUSTOMERS}")
    suspend fun getAllCustomers(): List<CustomerModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createItemsObjects(itemList: List<ItemsModel>)

    @Query("SELECT * FROM ${Constants.TABLE_ITEMS} WHERE ${Constants.COMPANY_NAME} = :companyType")
    suspend fun getAllItems(companyType: String): List<ItemsModel>

}