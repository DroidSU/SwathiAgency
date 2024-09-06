package com.sujoy.swathiagency.utilities

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.google.firebase.storage.FirebaseStorage
import com.sujoy.swathiagency.data.datamodels.CustomerModel
import com.sujoy.swathiagency.data.datamodels.ITCItemsModel
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

        fun setBillNumber(context: Context, billNumber: Long, billId: String?, companyType: String) {
            val sharedPref =
                context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            if(companyType == Constants.COMPANY_TYPE_ITC){
                editor.putLong(Constants.SHARED_PREF_BILL_NUMBER_ITC, billNumber)
                editor.putString(Constants.SHARED_PREF_BILL_ID_ITC, billId)
            }
            else if(companyType == Constants.COMPANY_TYPE_AVT){
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

        fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }

        fun calculateItemTotalValue(item: ITCItemsModel) {
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
        data: List<ITCItemsModel>,
        customerModel: CustomerModel,
        companyType: String
    ): File? {
        try {
            val timeStamp = SimpleDateFormat("ddMMyyyy", Locale.getDefault()).format(Date())
            val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

            val billNumber = getBillNumber(context, companyType)
            val billId = getBillId(context, companyType)
            val salesmanName = getSalesmanName(context)

            var fileName = ""

            fileName = when (companyType) {
                Constants.COMPANY_TYPE_ITC -> {
                    "ITC_$timeStamp.csv"
                }

                Constants.COMPANY_TYPE_AVT -> {
                    "AVT_$timeStamp.csv"
                }

                else -> {
                    "OTHERS_$timeStamp.csv"
                }
            }

            val file: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // App-specific external storage for Android 10 and above
                File(context.getExternalFilesDir(null), fileName)
            } else {
                // Internal storage for devices below Android 10
                File(context.filesDir, fileName)
            }

            val fileExists = file.exists()

            csvWriter().open(file, true) {

                // Write the header
//                if (!fileExists) {
//                    writeRow(
//                        listOf(
//                            "Invoice Number",
//                            "Cash Sales",
//                            "Sales Account",
//                            "Company Name",
//                            "Salesman",
//                            "Route",
//                            "Date",
//                            "Customer Name",
//                            "Item Name",
//                            "Box Price",
//                            "Box",
//                            "Pcs",
//                            "default",
//                            "default",
//                            "default",
//                            "default",
//                            "Tax Rate",
//                            "Category",
//                            "Pcs Rate"
//                        )
//                    )
//                }

                // Write each object as a row in the CSV
                data.forEach { item ->
                    writeRow(
                        listOf(
                            "$billId-$billNumber",
                            "N",
                            "Sales Account",
                            "ITC",
                            salesmanName,
                            customerModel.customerRoute,
                            date,
                            customerModel.customerName,
                            item.itemName,
                            item.taxableBoxRate,
                            item.numberOfBoxesOrdered,
                            item.numberOfPcsOrdered,
                            "0",
                            "0",
                            "0",
                            "0",
                            item.taxPercentage,
                            item.itemGroup,
                            item.taxablePcsRate
                        )
                    )
                }
            }

            context.getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE)
            return file
        } catch (ex: Exception) {
            Log.e("File not created", "createCsvFile: $ex")
            return null
        }
    }

    // Upload the CSV file to Firebase Storage
    suspend fun uploadCsvFile(context: Context, csvFile: File): Uri? {
        val storageReference = FirebaseStorage.getInstance().reference
        val date = getCurrentDateString("dd-MM-yyyy")
        val salesmanName = getSalesmanName(context)

        return try {
            // Create a reference to the file you want to upload
            val fileUri = Uri.fromFile(csvFile)
            val storageRef = storageReference.child("BACKUP/${salesmanName}/$date/${csvFile.name}")

            // Upload the file
            val uploadTask = storageRef.putFile(fileUri).await()

            // Get the download URL
            storageRef.downloadUrl.await()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}