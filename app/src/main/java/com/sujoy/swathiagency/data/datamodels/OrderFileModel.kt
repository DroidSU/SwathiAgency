package com.sujoy.swathiagency.data.datamodels

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "FileObjects")
data class OrderFileModel(
    @PrimaryKey(autoGenerate = false)
    val fileName: String,
    val createdBy: String,
    val createdOn: String,
    val isBackedUp : Int = 0,
    val fileURI : String,
    val companyName : String,
    val customerName : String,
    val orderId : String,
)