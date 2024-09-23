package com.sujoy.swathiagency.utilities

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.google.firebase.storage.FirebaseStorage
import com.sujoy.swathiagency.data.datamodels.CustomerModel
import com.sujoy.swathiagency.data.datamodels.CustomerOrderModel
import com.sujoy.swathiagency.data.datamodels.ItemsModel
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class UtilityMethods {
    companion object {
        fun hideKeyBoard(view: View, context: Context) {
            // Function to hide the keyboard
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fun showAlertDialog(
            context: Context,
            dialogMessage: String,
            onPositiveClicked: () -> Unit,
            onNegativeClicked: () -> Unit,
        ) {
            val builder = AlertDialog.Builder(context)
//            builder.setTitle("Alert Dialog")
            builder.setMessage(dialogMessage)

            // Positive button
            builder.setPositiveButton("OK") { dialog, which ->
                // Handle OK button click
                onPositiveClicked()
            }

            // Negative button
            builder.setNegativeButton("Cancel") { dialog, which ->
                // Handle Cancel button click
                dialog.dismiss()
            }

            // Create and show the AlertDialog
            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()
        }

        fun setBillNumber(
            context: Context,
            billNumber: Long,
            billId: String?,
            companyType: String
        ) {
            val sharedPref =
                context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            if (companyType == Constants.COMPANY_TYPE_ITC) {
                editor.putLong(Constants.SHARED_PREF_BILL_NUMBER_ITC, billNumber)
                editor.putString(Constants.SHARED_PREF_BILL_ID_ITC, billId)
            } else if (companyType == Constants.COMPANY_TYPE_AVT) {
                editor.putLong(Constants.SHARED_PREF_BILL_NUMBER_AVT, billNumber)
                editor.putString(Constants.SHARED_PREF_BILL_ID_AVT, billId)
            }
            editor.apply()
        }

        fun getBillNumber(context: Context, companyType: String): Long {
            return when (companyType) {
                Constants.COMPANY_TYPE_ITC -> {
                    context.getSharedPreferences(
                        Constants.SHARED_PREF_NAME,
                        Context.MODE_PRIVATE
                    )
                        .getLong(Constants.SHARED_PREF_BILL_NUMBER_ITC, 0L)
                }

                Constants.COMPANY_TYPE_AVT -> {
                    context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                        .getLong(Constants.SHARED_PREF_BILL_NUMBER_AVT, 0L)
                }

                else -> 0L
            }
        }

        fun getBillId(context: Context, companyType: String): String? {
            return when (companyType) {
                Constants.COMPANY_TYPE_ITC -> {
                    context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                        .getString(Constants.SHARED_PREF_BILL_ID_ITC, "")
                }

                Constants.COMPANY_TYPE_AVT -> {
                    context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                        .getString(Constants.SHARED_PREF_BILL_ID_AVT, "")
                }

                else -> ""
            }
        }

        fun setSalesmanName(context: Context, salesmanName: String) {
            val sharedPRef =
                context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
            val editor = sharedPRef.edit()
            editor.putString(Constants.SHARED_PREF_SALESMAN_NAME, salesmanName)
            editor.apply()
        }

        fun getSalesmanName(context: Context): String {
            return context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .getString(Constants.SHARED_PREF_SALESMAN_NAME, "")
                .toString()
        }

        fun setSelectedRoute(context: Context, selectedRoute: String) {
            val sharedPref =
                context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString(Constants.SHARED_PREF_SELECTED_ROUTE, selectedRoute)
            editor.apply()
        }

        fun getSelectedRoute(context: Context): String {
            return context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .getString(Constants.SHARED_PREF_SELECTED_ROUTE, "").toString()
        }

        fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }

        fun calculateItemTotalValue(item: ItemsModel) {
            var total = 0F

            val boxTotal = item.numberOfBoxesOrdered.toFloat() * item.taxableBoxRate.toFloat()
            val pcsTotal = item.numberOfPcsOrdered.toFloat() * item.taxablePcsRate.toFloat()
            val totalTaxableValue = boxTotal + pcsTotal

            val taxApplied = totalTaxableValue * (item.taxPercentage.toFloat() / 100)
            total = totalTaxableValue + taxApplied

            item.taxable = totalTaxableValue
            item.taxAmount = taxApplied
            item.totalAmount = total
        }

        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }

        fun getCurrentDateString(pattern: String): String {
            return SimpleDateFormat(pattern, Locale.getDefault()).format(Date())
        }
    }

    fun createOrUpdateCsvFile(
        context: Context,
        data: List<ItemsModel>,
        customerModel: CustomerModel,
        companyType: String,
        timestamp: String
    ): File? {
        try {
            val date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

            val billNumber = getBillNumber(context, companyType)
            val billId = getBillId(context, companyType)
            val salesmanName = getSalesmanName(context)

            val fileName: String = when (companyType) {
                Constants.COMPANY_TYPE_ITC -> {
                    "ITC_${salesmanName}_${getCurrentDateString("ddMMyyyy")}.csv"
                }

                Constants.COMPANY_TYPE_AVT -> {
                    "AVT_${salesmanName}_${getCurrentDateString("ddMMyyyy")}.csv"
                }

                else -> {
                    "OTHERS_${getCurrentDateString("ddMMyyyy")}.csv"
                }
            }

            val file: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // App-specific external storage for Android 10 and above
                File(context.getExternalFilesDir(null), fileName)
            } else {
                // Internal storage for devices below Android 10
                File(context.filesDir, fileName)
            }

            csvWriter().open(file, true) {

                // Write each object as a row in the CSV
                data.forEach { item ->
                    writeRow(
                        listOf(
                            "$billId-$billNumber",
                            "N",
                            "Sales Account",
                            companyType,
                            salesmanName,
                            customerModel.customerRoute,
                            date,
                            customerModel.customerName,
                            item.itemName,
                            item.taxableBoxRate.toFloat(),
                            item.numberOfBoxesOrdered,
                            item.numberOfPcsOrdered,
                            "0",
                            "0",
                            "0",
                            "0",
                            item.taxPercentage.toFloat(),
                            item.itemGroup,
                            item.taxablePcsRate.toFloat()
                        )
                    )
                }
            }

            Log.d("File created", "File URI : ${UtilityMethods().getFileUri(context, file)}")
            return file
        } catch (ex: Exception) {
            Log.e("File not created", "createCsvFile: $ex")
            return null
        }
    }

    fun createCustomerOrdersBackupCSV(context: Context, data: List<CustomerOrderModel>, companyType: String, totalValue : Long): File? {
        val salesmanName = getSalesmanName(context)
        val fileName = "${companyType}_${salesmanName}_${totalValue}.csv"
        val file: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // App-specific external storage for Android 10 and above
            File(context.getExternalFilesDir(null), fileName)
        } else {
            // Internal storage for devices below Android 10
            File(context.filesDir, fileName)
        }

        try {
            csvWriter().open(file, file.exists()) {
                writeRow(listOf("Date", "Customer Name", "Total Value"))
                data.forEach { item ->
                    writeRow(item.date, item.customerName, item.orderTotal)
                }
            }

            Log.d("Orders Total CSV created", "File created in ${getFileUri(context, file)}")
            return file
        } catch (ex: Exception) {
            Log.e("File not created", "createCsvFile: $ex")
            return null
        }
    }


    // Upload the CSV file to Firebase Storage
    suspend fun backupOrdersCSVFile(context: Context, csvFile: File, fileURI: Uri): Uri? {
        Log.d("File", "Getting file from $fileURI")
        val storageReference = FirebaseStorage.getInstance().reference
        val date = getCurrentDateString("dd-MM-yyyy")
        val currentTime = getCurrentDateString("hh:mm a")
//        val salesmanName = getSalesmanName(context)

        return try {
            val storageRef =
                storageReference.child("BACKUP/$date/$currentTime/${csvFile.name}")
            val uploadTask = storageRef.putFile(fileURI).await()
            storageRef.downloadUrl.await()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun backupCustomerOrderCSV(context: Context, csvFile: File, fileURI : Uri): Uri? {
        val storageReference = FirebaseStorage.getInstance().reference
        val date = getCurrentDateString("dd-MM-yyyy")
        val currentTime = getCurrentDateString("hh:mm a")
//        val salesmanName = getSalesmanName(context)

        return try {
            val storageRef =
                storageReference.child("BACKUP/$date/$currentTime/${csvFile.name}")
            val uploadTask = storageRef.putFile(fileURI).await()
            return storageRef.downloadUrl.await()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getFileUri(context: Context, file: File): Uri {
        // Use FileProvider to generate a content URI
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",  // Authority defined in the manifest
            file
        )
    }

}