package com.sujoy.swathiagency.data.datamodels

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity("Orders")
@Parcelize
data class OrdersTable(
    @PrimaryKey(autoGenerate = false)
    val orderId : String,
    val companyName : String,
    var orderTotal : Float = 0F,
    val createdDate : String,
    val orderedItemList : List<ItemsModel>,
    val orderFileName : String = "",
    val isBackedUp : Boolean = false,
    val fileURI : String = "",
    val customerModel : CustomerModel
) : Parcelable
