package com.sujoy.swathiagency.network

import android.content.Context
import android.util.Log
import com.sujoy.swathiagency.data.CustomerModel
import com.sujoy.swathiagency.data.ITCItemsModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NetworkRepository(private val context: Context) {

    suspend fun fetchCustomerCSV(fileUrl: String): ArrayList<CustomerModel> {
        val customerModelList: ArrayList<CustomerModel> = ArrayList()

        return withContext(Dispatchers.IO) {
            val response = RetrofitClient.getRetrofitInstanceWithoutAccessToken()
                .create(GoogleDriveService::class.java).downloadCustomerFile(fileUrl)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    val csvContent = body.string() // Get the response as a String
                    val rows = csvContent.split("\n").map { it.split(",") }
                    rows.drop(1)

                    rows.forEachIndexed { index, item ->
                        if (item.isNotEmpty()) {
                            try {
                                if (item[0].isNotEmpty() && item[2].trim().isNotEmpty()) {
                                    val customerName = item[0]
                                    val customerRoute = item[1]
                                    val customerAmount = item[2].trim().toFloat()

                                    customerModelList.add(
                                        CustomerModel(
                                            customerName = customerName,
                                            customerRoute = customerRoute,
                                            customerAmount = customerAmount
                                        )
                                    )
                                }
                            } catch (exception: Exception) {
                                Log.e(
                                    "CSV Parsing Exception",
                                    "Exception ${exception.toString()}"
                                )
                            }
                        }
                    }

                    customerModelList
                } ?: ArrayList()
            } else {
                ArrayList()
            }
        }
    }

    suspend fun fetchITCItemsCSV(fileUrl: String): ArrayList<ITCItemsModel> {
        val itemsList: ArrayList<ITCItemsModel> = ArrayList()

        return withContext(Dispatchers.IO) {
            val response = RetrofitClient.getRetrofitInstanceWithoutAccessToken()
                .create(GoogleDriveService::class.java).getITCItemFile(fileUrl)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    val csvContent = body.string() // Get the response as a String
                    val rows = csvContent.split("\n").map { it.split(",") }
                    rows.drop(1)

                    Log.d("ITC ITEM Response", "fetchITCItemsCSV: ${rows.toString()}")

                    rows.forEachIndexed { index, item ->
                        if (item.isNotEmpty() && item.all { it.isNotEmpty() }) {
                            itemsList.add(
                                ITCItemsModel(
                                    itemName = item[0],
                                    availableQuantity = item[1],
                                    numberOfPcs = item[2],
                                    taxableBoxRate = item[3],
                                    taxPercentage = item[4],
                                    mrp = item[5],
                                    itemGroup = item[6],
                                    taxablePcsRate = item[13]
                                )
                            )
                        }
                    }
                } ?: arrayListOf(ITCItemsModel::class.java)
            }
            itemsList
        }
    }
}