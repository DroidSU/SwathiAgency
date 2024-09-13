package com.sujoy.swathiagency.data.dbModels

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CustomerOrder")
data class CustomerOrderModel(
    @PrimaryKey(autoGenerate = false)
    val orderId : String,
    val customerName : String,
    var orderTotal : Float = 0F,
    val date : String,
    val companyName : String,
)
