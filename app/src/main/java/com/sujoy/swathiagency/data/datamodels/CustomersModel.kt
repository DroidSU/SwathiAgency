package com.sujoy.swathiagency.data.datamodels

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity("Customers")
@Parcelize
data class CustomerModel(@PrimaryKey(autoGenerate = false) val customerId : Int, val customerName : String, val customerRoute : String, val customerAmount : Float) : Parcelable