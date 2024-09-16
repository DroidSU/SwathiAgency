package com.sujoy.swathiagency.data.datamodels

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity("Items")
@Parcelize
data class ItemsModel(
    @PrimaryKey(autoGenerate = false) var itemID : String,
    val itemName: String,
    var availableQuantity: String,
    var numberOfPcs: String,
    val taxableBoxRate: String,
    val taxPercentage: String,
    val mrp: String,
    val itemGroup: String,
    val taxablePcsRate: String,
    var numberOfBoxesOrdered : Int = 0,
    var numberOfPcsOrdered : Int = 0,
    var totalAmount : Float = 0.0F,
    var taxable : Float = 0.0F,
    var taxAmount : Float = 0.0F,
    var selected : Boolean = false,
    var companyName : String
) : Parcelable
