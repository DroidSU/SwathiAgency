package com.sujoy.swathiagency.network

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.sujoy.swathiagency.data.datamodels.CustomerModel
import com.sujoy.swathiagency.data.datamodels.ItemsModel
import com.sujoy.swathiagency.utilities.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File

class NetworkRepository(private val context: Context) {

    suspend fun downloadCustomerCSV(): ArrayList<CustomerModel> {
        val customerModelList: ArrayList<CustomerModel> = arrayListOf()
        val storageRef = FirebaseStorage.getInstance().reference

        return withContext(Dispatchers.IO) {
            val storageFileRef = storageRef.child("SYNC/CUST.csv")
            val inputStream = storageFileRef.stream.await().stream
            val csvString = inputStream.bufferedReader().use(BufferedReader::readText)

            val localFile = File(context.filesDir, Constants.LOCAL_CUSTOMER_FILE_NAME)
            if (localFile.exists()) {
                localFile.delete()
            }
            localFile.createNewFile()

            storageRef.getFile(localFile).addOnSuccessListener {
                // File downloaded successfully
                Log.d("File downloaded", "File downloaded to: ${localFile.absolutePath}")
            }.addOnFailureListener { exception ->
                // Handle any errors
                println("Download failed: ${exception.message}")
            }


            // Split the string into lines
            val rows = csvString.split("\n")

            // Loop through each row and split by commas, skipping the header
            rows.drop(1).forEach { row ->
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

    suspend fun getLocalCustomerCSV(): ArrayList<CustomerModel> {
        val customerModelList: ArrayList<CustomerModel> = arrayListOf()

        return withContext(Dispatchers.IO) {
            val file: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // App-specific external storage for Android 10 and above
                File(context.getExternalFilesDir(null), Constants.LOCAL_CUSTOMER_FILE_NAME)
            } else {
                // Internal storage for devices below Android 10
                File(context.filesDir, Constants.LOCAL_CUSTOMER_FILE_NAME)
            }

            if (file.exists()) {
                file.bufferedReader().use { reader ->
                    // Skip the first line if it contains headers
                    val lines = reader.readLines().drop(1)

                    for (line in lines) {
                        // Split the line by commas to get each column's value
                        val columns = line.split(",")
                        if (columns.size >= 3) {
                            val customerName = columns[0].trim()
                            val customerRoute = columns[1].trim()
                            val customerAmount =
                                columns[2].trim().toFloat()  // Default age to 0 if invalid

                            // Add the object to the list
                            customerModelList.add(
                                CustomerModel(
                                    customerName,
                                    customerRoute,
                                    customerAmount
                                )
                            )
                        }
                    }
                }
            }

            customerModelList
        }
    }

    suspend fun downloadItemsCSV(companyType: String): ArrayList<ItemsModel> {
        val itemsList: ArrayList<ItemsModel> = ArrayList()
        val storageRef = FirebaseStorage.getInstance().reference

        return withContext(Dispatchers.IO) {
            val storageFileRef = storageRef.child("SYNC/ITC.csv")
            val inputStream = storageFileRef.stream.await().stream
            val csvString = inputStream.bufferedReader().use(BufferedReader::readText)

            val localFile: File = if (companyType == Constants.COMPANY_TYPE_ITC) {
                File(context.filesDir, Constants.LOCAL_ITC_FILE_NAME)
            } else {
                File(context.filesDir, Constants.LOCAL_AVT_FILE_NAME)
            }

            if (localFile.exists()) {
                localFile.delete()
            }
            localFile.createNewFile()

            storageRef.getFile(localFile).addOnSuccessListener {
                // File downloaded successfully
                Log.d("File downloaded", "File downloaded to: ${localFile.absolutePath}")
            }.addOnFailureListener { exception ->
                // Handle any errors
                println("Download failed: ${exception.message}")
            }

            val rows = csvString.split("\n").map { it.split(",") }
            rows.drop(1)
            rows.forEachIndexed { index, item ->
                if (item.isNotEmpty() && item.all { it.isNotEmpty() }) {
                    itemsList.add(
                        ItemsModel(
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

            itemsList
        }
    }

    suspend fun getLocalItemsCSV(companyType: String): ArrayList<ItemsModel> {
        val itemsList: ArrayList<ItemsModel> = arrayListOf()

        return withContext(Dispatchers.IO) {
            val fileName: String = if (companyType == Constants.COMPANY_TYPE_ITC) {
                Constants.LOCAL_ITC_FILE_NAME
            } else {
                Constants.LOCAL_AVT_FILE_NAME
            }

            val file: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // App-specific external storage for Android 10 and above
                File(context.getExternalFilesDir(null), fileName)
            } else {
                // Internal storage for devices below Android 10
                File(context.filesDir, fileName)
            }

            if (file.exists()) {
                file.bufferedReader().use { reader ->
                    // Skip the first line if it contains headers

                    val lines = reader.readLines().drop(1)

                    for (line in lines) {
                        // Split the line by commas to get each column's value
                        val item = line.split(",")
                        if (item.size >= 3) {
                            itemsList.add(
                                ItemsModel(
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
                }
            }

            itemsList
        }
    }

}