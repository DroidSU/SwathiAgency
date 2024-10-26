package com.sujoy.swathiagency.ui.collections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import com.sujoy.swathiagency.data.datamodels.CollectionsCustomerModel
import com.sujoy.swathiagency.databinding.LayoutCashCollectionEditBinding
import com.sujoy.swathiagency.interfaces.OnCashEditDialogDismissed
import com.sujoy.swathiagency.interfaces.OnCashEditSubmitTapped
import com.sujoy.swathiagency.utilities.UtilityMethods.Companion.dpToPx

class CashCollectionDialog(
    private var collectionCustomerModel: CollectionsCustomerModel,
    private var onCashEditDialogDismissed: OnCashEditDialogDismissed,
    private var onCashEditSubmit: OnCashEditSubmitTapped
) : DialogFragment() {

    private lateinit var binding: LayoutCashCollectionEditBinding

    private var couponDiscount = 0.0F
    private var billDiscount = 0.0F
    private var displayDiscount = 0.0F
    private var saleReturnValue = 0.0F
    private var otherDiscounts = 0.0F
    private var amountCollected = 0.0F
    private var remarks = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LayoutCashCollectionEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etCouponDiscount.setText(collectionCustomerModel.couponDiscount.toString())
        binding.etDisplayDiscount.setText(collectionCustomerModel.displayDiscount.toString())
        binding.etBillDiscount.setText(collectionCustomerModel.billDiscount.toString())
        binding.etSaleReturnValue.setText(collectionCustomerModel.saleReturnValue.toString())
        binding.etOtherDiscounts.setText(collectionCustomerModel.otherDiscounts.toString())
        amountCollected =
            collectionCustomerModel.dueAmount - (couponDiscount + billDiscount + displayDiscount + saleReturnValue + otherDiscounts)
        binding.etAmountCollected.setText(amountCollected.toString())
        binding.etRemarks.setText(collectionCustomerModel.remarks)

        binding.etCouponDiscount.doAfterTextChanged {
            val inputText = it.toString()
            couponDiscount = if (inputText.isNotEmpty() && inputText != ".") {
                inputText.toFloat()
            } else {
                0f // Default to 0 if input is empty or only contains "."
            }

            amountCollected =
                collectionCustomerModel.dueAmount - (couponDiscount + billDiscount + displayDiscount + saleReturnValue + otherDiscounts)
            binding.etAmountCollected.setText(amountCollected.toString())
        }

        binding.etBillDiscount.doAfterTextChanged {
            val inputText = it.toString()
            billDiscount = if (inputText.isNotEmpty() && inputText != ".") {
                inputText.toFloat()
            } else {
                0f // Default to 0 if input is empty or only contains "."
            }

            amountCollected =
                collectionCustomerModel.dueAmount - (couponDiscount + billDiscount + displayDiscount + saleReturnValue + otherDiscounts)
            binding.etAmountCollected.setText(amountCollected.toString())
        }

        binding.etDisplayDiscount.doAfterTextChanged {
            val inputText = it.toString()
            displayDiscount = if (inputText.isNotEmpty() && inputText != ".") {
                inputText.toFloat()
            } else {
                0f // Default to 0 if input is empty or only contains "."
            }

            amountCollected =
                collectionCustomerModel.dueAmount - (couponDiscount + billDiscount + displayDiscount + saleReturnValue + otherDiscounts)
            binding.etAmountCollected.setText(amountCollected.toString())
        }

        binding.etSaleReturnValue.doAfterTextChanged {
            val inputText = it.toString()
            saleReturnValue = if (inputText.isNotEmpty() && inputText != ".") {
                inputText.toFloat()
            } else {
                0f // Default to 0 if input is empty or only contains "."
            }

            amountCollected =
                collectionCustomerModel.dueAmount - (couponDiscount + billDiscount + displayDiscount + saleReturnValue + otherDiscounts)
            binding.etAmountCollected.setText(amountCollected.toString())
        }

        binding.etOtherDiscounts.doAfterTextChanged {
            val inputText = it.toString()
            otherDiscounts = if (inputText.isNotEmpty() && inputText != ".") {
                inputText.toFloat()
            } else {
                0f // Default to 0 if input is empty or only contains "."
            }

            amountCollected =
                collectionCustomerModel.dueAmount - (couponDiscount + billDiscount + displayDiscount + saleReturnValue + otherDiscounts)
            binding.etAmountCollected.setText(amountCollected.toString())
        }

        binding.etAmountCollected.doAfterTextChanged {
//            val inputText = it.toString()
//            amountCollected = if (inputText.isNotEmpty() && inputText != ".") {
//                inputText.toFloat()
//            } else {
//                0f // Default to 0 if input is empty or only contains "."
//            }
//
//            amountCollected =
//                collectionCustomerModel.dueAmount - (couponDiscount + billDiscount + displayDiscount + saleReturnValue + otherDiscounts)
//            binding.etAmountCollected.setText(amountCollected.toString())
        }

        binding.etRemarks.doAfterTextChanged {
            if (it.toString().isNotEmpty()) {
                remarks = it.toString()
            }
        }


        binding.ivDismissDialog.setOnClickListener {
            onCashEditDialogDismissed.onCashEditDialogDismissed()
        }

        binding.btnCompleteEdit.setOnClickListener {
            collectionCustomerModel.couponDiscount = couponDiscount
            collectionCustomerModel.billDiscount = billDiscount
            collectionCustomerModel.displayDiscount = displayDiscount
            collectionCustomerModel.saleReturnValue = saleReturnValue
            collectionCustomerModel.otherDiscounts = otherDiscounts
            collectionCustomerModel.amountCollected = amountCollected
            collectionCustomerModel.remarks = remarks

            onCashEditSubmit.onCashEditSubmitTapped(collectionCustomerModel)
        }
    }


    override fun onStart() {
        super.onStart()

        dialog?.window?.setLayout(
            400.dpToPx(
                requireContext()
            ), 450.dpToPx(requireContext())
        )
    }
}