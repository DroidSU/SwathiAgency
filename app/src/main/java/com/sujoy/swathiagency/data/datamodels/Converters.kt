package com.sujoy.swathiagency.data.datamodels

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    @TypeConverter
    fun fromItemList(value: List<ItemsModel>?): String {
        val gson = Gson()
        return gson.toJson(value)
    }

    @TypeConverter
    fun toItemList(value: String): List<ItemsModel>? {
        val listType = object : TypeToken<List<ItemsModel>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromCustomerModel(value: CustomerModel?): String {
        val gson = Gson()
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCustomerModel(value: String): CustomerModel? {
        val objectType = object : TypeToken<CustomerModel>() {}.type
        return Gson().fromJson(value, objectType)
    }
}