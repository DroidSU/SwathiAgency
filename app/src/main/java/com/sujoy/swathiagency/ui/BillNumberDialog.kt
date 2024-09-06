package com.sujoy.swathiagency.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.sujoy.swathiagency.databinding.DialogBillNumberInputBinding
import com.sujoy.swathiagency.interfaces.OnSubmitButtonTapped
import com.sujoy.swathiagency.utilities.UtilityMethods.Companion.dpToPx

class BillNumberDialog(private var onSubmitButtonTapped: OnSubmitButtonTapped) : DialogFragment() {

    private lateinit var binding: DialogBillNumberInputBinding
    private var billNumber: Long = 0
    private var billId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogBillNumberInputBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSubmitBillNumber.setOnClickListener {
            billId = binding.etBillId.text.toString()
            if (billId.isNotEmpty() && binding.etBillNumber.text.isNotEmpty()) {
                billNumber = binding.etBillNumber.text.toString().toLong()
                dismiss()
                onSubmitButtonTapped.onSubmitButtonTap(billId, billNumber)
            } else {
                binding.etBillId.error = "Please enter bill number to continue"
                binding.etBillNumber.error = "Please enter bill number to continue"
            }
        }
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.setLayout(
            300.dpToPx(
                requireContext()
            ), 250.dpToPx(requireContext()) // Convert 200dp to pixels
        )
    }
}