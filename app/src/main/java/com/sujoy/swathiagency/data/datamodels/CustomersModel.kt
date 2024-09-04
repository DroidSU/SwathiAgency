package com.sujoy.swathiagency.data.datamodels

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CustomerModel(val customerName : String, val customerRoute : String, val customerAmount : Float) : Parcelable