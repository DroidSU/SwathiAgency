package com.sujoy.swathiagency.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sujoy.swathiagency.data.dbModels.FileObjectModels
import com.sujoy.swathiagency.utilities.Constants

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createOrder(orderModel: FileObjectModels)

    @Query("SELECT * FROM ${Constants.TABLE_FILE_OBJECTS} where ${Constants.IS_BACKED_UP} = 0 AND ${Constants.COMPANY_NAME} = :companyType")
    suspend fun getOrdersNotBackedUp(companyType: String) : List<FileObjectModels>

    @Query("SELECT EXISTS(SELECT 1 FROM ${Constants.TABLE_FILE_OBJECTS} WHERE ${Constants.CREATED_ON_DATE} = :dateString AND ${Constants.COMPANY_NAME} = :companyType)")
    suspend fun isDatePresent(dateString : String, companyType : String) : Int

    @Query("UPDATE ${Constants.TABLE_FILE_OBJECTS} SET ${Constants.IS_BACKED_UP} = 1 where ${Constants.COMPANY_NAME} = :companyName")
    suspend fun updateIsBackedUp(companyName: String)
}