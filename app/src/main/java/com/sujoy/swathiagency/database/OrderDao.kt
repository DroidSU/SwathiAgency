package com.sujoy.swathiagency.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sujoy.swathiagency.data.datamodels.CustomerModel
import com.sujoy.swathiagency.data.datamodels.ItemsModel
import com.sujoy.swathiagency.data.datamodels.OrdersTable
import com.sujoy.swathiagency.utilities.Constants

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createCustomerObject(customerModel: List<CustomerModel>)

    @Query("SELECT * FROM ${Constants.TABLE_CUSTOMERS}")
    suspend fun getAllCustomers(): List<CustomerModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createItemsObjects(itemList: List<ItemsModel>)

    @Query("SELECT * FROM ${Constants.TABLE_ITEMS} WHERE ${Constants.COMPANY_NAME} = :companyType")
    suspend fun getAllItems(companyType: String): List<ItemsModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createOrderObject(orderObject : OrdersTable)

    @Query("SELECT * FROM ${Constants.TABLE_ORDERS} where ${Constants.COMPANY_NAME} = :companyType AND ${Constants.IS_BACKED_UP} = 0")
    suspend fun getCompanyOrdersNotBackedUp(companyType: String): List<OrdersTable>

    @Query("SELECT * FROM ${Constants.TABLE_ORDERS} where ${Constants.IS_BACKED_UP} = 0")
    suspend fun getAllOrdersNotBackedUp(): List<OrdersTable>

    @Query("UPDATE ${Constants.TABLE_ORDERS} SET ${Constants.IS_BACKED_UP} = 1 where ${Constants.ORDER_ID} = :orderId")
    suspend fun setIsBackedUp(orderId: String)
}