package com.sujoy.swathiagency.data.datamodels

import androidx.room.PrimaryKey

data class CollectionsCustomerModel(
    @PrimaryKey(autoGenerate = false)
    val customerId : Int,
    val customerName : String,
    val companyName : String,
    val route : String,
    var invoiceDate : String,
    var invoiceNumber : String,
    var invoiceAmount : Float,
    var received : Float,
    var dueAmount : Float,
    var dueDays : Int,
    var couponDiscount : Float = 0f,
    var billDiscount : Float = 0f,
    var displayDiscount : Float = 0f,
    var saleReturnValue : Float = 0f,
    var otherDiscounts : Float = 0f,
    var amountCollected : Float = 0f,
    var remarks : String = "",
)
