package com.sujoy.swathiagency.data.dbModels

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "FileObjects")
data class FileObjectModels(
    @PrimaryKey(autoGenerate = false)
    val fileName: String,
    val createdBy: String,
    val createdOn: String,
    val isBackedUp : Int = 0,
    val fileURI : String,
    val companyName : String
)