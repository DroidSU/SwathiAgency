package com.sujoy.swathiagency.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ITCItemsModel(
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
    var selected : Boolean = false
) : Parcelable
