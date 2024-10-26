package com.sujoy.swathiagency.network

import android.content.Context
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.sujoy.swathiagency.data.datamodels.CollectionsCustomerModel
import com.sujoy.swathiagency.data.datamodels.CustomerModel
import com.sujoy.swathiagency.data.datamodels.ItemsModel
import com.sujoy.swathiagency.utilities.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.BufferedReader

class NetworkRepository(private val context: Context) {

    suspend fun getCustomerCSV(): ArrayList<CustomerModel> {
        val customerModelList: ArrayList<CustomerModel> = arrayListOf()
        val storageRef = FirebaseStorage.getInstance().reference

        return withContext(Dispatchers.IO) {
            val storageFileRef = storageRef.child("SYNC/CUST.csv")
            val inputStream = storageFileRef.stream.await().stream
            val csvString = inputStream.bufferedReader().use(BufferedReader::readText)

            // Split the string into lines
            val rows = csvString.split("\n")

            // Loop through each row and split by commas, skipping the header
            rows.drop(1).forEachIndexed { index, row ->
                val columns = row.split(",")
                try {
                    if (columns.size >= 3) {
                        val customerName = columns[0].trim()
                        val customerRoute = columns[1].trim()
                        val customerAmount = columns[2].trim()
                            .toFloat()  // Convert age to int, default to 0 if invalid

                        // Add to the list if valid
                        customerModelList.add(
                            CustomerModel(
                                customerId = index + 1,
                                customerName = customerName,
                                customerRoute = customerRoute,
                                customerAmount = customerAmount
                            )
                        )
                    }
                } catch (ex: Exception) {
                    Log.e(
                        "CSV Parsing Exception",
                        "Exception $ex"
                    )
                }
            }
            customerModelList
        }
    }

    suspend fun getItemsCSV(companyType: String): ArrayList<ItemsModel> {
        val itemsList: ArrayList<ItemsModel> = ArrayList()
        val storageRef = FirebaseStorage.getInstance().reference

        return withContext(Dispatchers.IO) {
            val storageFileRef =
                if (companyType == Constants.COMPANY_TYPE_ITC) storageRef.child("SYNC/ITC.csv") else storageRef.child(
                    "SYNC/AVT.csv"
                )
            val inputStream = storageFileRef.stream.await().stream
            val csvString = inputStream.bufferedReader().use(BufferedReader::readText)

            val rows = csvString.split("\n").map { it.split(",") }
            rows.drop(1)
            rows.forEachIndexed { index, item ->
                if (item.isNotEmpty() && item.all { it.isNotEmpty() }) {
                    itemsList.add(
                        ItemsModel(
                            itemID = "${companyType}_${index + 1}",
                            itemName = item[0],
                            availableQuantity = item[1].trim(),
                            numberOfPcs = item[2].trim(),
                            taxableBoxRate = item[3].trim(),
                            taxPercentage = item[4].trim(),
                            mrp = item[5].trim(),
                            itemGroup = item[6].trim(),
                            taxablePcsRate = item[13].trim(),
                            companyName = companyType
                        )
                    )
                }
            }

            itemsList
        }
    }

    suspend fun getCollectionsCSV(): ArrayList<CollectionsCustomerModel> {
        val customerModelList: ArrayList<CollectionsCustomerModel> = arrayListOf()
        val storageRef = FirebaseStorage.getInstance().reference

        return withContext(Dispatchers.IO) {
            val storageFileRef = storageRef.child("SYNC/OUT.csv")
            val inputStream = storageFileRef.stream.await().stream
            val csvString = inputStream.bufferedReader().use(BufferedReader::readText)

            // Split the string into lines
            val rows = csvString.split("\n")

//             Loop through each row and split by commas, skipping the header
            rows.drop(1).forEachIndexed { index, row ->
                val columns = row.split(",")
                try {
                    if (columns.size >= 3) {
                        val customerName = columns[0].trim().replace("\"", "")
                        val companyName = columns[1].trim().replace("\"", "")
                        val customerRoute = columns[2].trim().replace("\"", "")
                        val invoiceDate = columns[3].trim().replace("\"", "")
                        val invoiceNumber = columns[4].trim().replace("\"", "")
                        val invoiceAmount =
                            if (columns[5].trim().isNotBlank()) columns[5].trim().replace("\"", "")
                                .toFloat() else 0f
                        val received =
                            if (columns[6].trim().isNotBlank()) columns[6].trim().replace("\"", "")
                                .toFloat() else 0f
                        val dueAmount =
                            if (columns[7].trim().isNotBlank()) columns[7].trim().replace("\"", "")
                                .toFloat() else 0f
                        val dueDays = columns.getOrNull(8)?.trim()?.replace("\"", "")?.toIntOrNull() ?: 0

                        // Add to the list if valid
                        customerModelList.add(
                            CollectionsCustomerModel(
                                customerId = index + 1,
                                customerName = customerName,
                                companyName = companyName,
                                route = customerRoute,
                                invoiceDate = invoiceDate,
                                invoiceNumber = invoiceNumber,
                                invoiceAmount = invoiceAmount,
                                received = received,
                                dueAmount = dueAmount,
                                dueDays = dueDays
                            )
                        )
                    }
                } catch (ex: Exception) {
                    Log.e(
                        "CSV Parsing Exception",
                        "${columns.toString()} Exception $ex"
                    )
                }
            }
            customerModelList
        }
    }

}