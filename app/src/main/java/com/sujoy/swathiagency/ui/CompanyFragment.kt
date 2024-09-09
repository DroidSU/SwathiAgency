package com.sujoy.swathiagency.ui

import android.R
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sujoy.swathiagency.adapters.ItemsRecyclerAdapter
import com.sujoy.swathiagency.data.datamodels.CustomerModel
import com.sujoy.swathiagency.data.datamodels.ItemsModel
import com.sujoy.swathiagency.databinding.FragmentItcBinding
import com.sujoy.swathiagency.interfaces.OnItemEvent
import com.sujoy.swathiagency.interfaces.OnSubmitButtonTapped
import com.sujoy.swathiagency.network.NetworkRepository
import com.sujoy.swathiagency.utilities.Constants
import com.sujoy.swathiagency.utilities.UtilityMethods
import com.sujoy.swathiagency.utilities.UtilityMethods.Companion.showAlertDialog
import com.sujoy.swathiagency.viewmodels.CompanyVMFactory
import com.sujoy.swathiagency.viewmodels.CompanyViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

class CompanyFragment : Fragment(), OnItemEvent, OnSubmitButtonTapped {

    companion object {
        const val CUSTOMER_MODEL_KEY = "CUSTOMER_MODEL"
        const val COMPANY_TYPE_KEY = "COMPANY_TYPE"
        const val TOTAL_BILL = "TOTAL_BILL"

        fun newInstance(companyType: String, customerModel: CustomerModel): CompanyFragment {
            val fragment = CompanyFragment()
            val args = Bundle()
            args.putString(COMPANY_TYPE_KEY, companyType)
            args.putParcelable(CUSTOMER_MODEL_KEY, customerModel)
            fragment.arguments = args
            return fragment
        }
    }

    private val viewModel: CompanyViewModel by viewModels {
        CompanyVMFactory(NetworkRepository(requireContext()))
    }

    private lateinit var binding: FragmentItcBinding

    private lateinit var selectedCustomer: CustomerModel
    private var categories: MutableList<String> = mutableListOf()
    private var categoriesAdapter: ArrayAdapter<String>? = null
    private var itemList: ArrayList<ItemsModel> = arrayListOf()
    private var selectedCategory: String = ""

    private lateinit var itemsRecyclerAdapter: ItemsRecyclerAdapter
    private lateinit var lottieOverlayFragment: LottieOverlayFragment

    private lateinit var companyType: String
    private var billNumber = MutableLiveData<Long>()
    private var billDialog: BillNumberDialog? = null
    private var salesmanName: String = ""
    private var billId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentItcBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility", "NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedCustomer = arguments?.getParcelable(CUSTOMER_MODEL_KEY)!!
        companyType = arguments?.getString(COMPANY_TYPE_KEY, Constants.COMPANY_TYPE_ITC)!!

        viewModel.setCustomerModel(selectedCustomer)

        lottieOverlayFragment = LottieOverlayFragment.newInstance()

        categoriesAdapter =
            ArrayAdapter(requireContext(), R.layout.simple_dropdown_item_1line, categories)
        binding.searchCategoryDropdown.setAdapter(categoriesAdapter)

        binding.recyclerCategoryItems.layoutManager = LinearLayoutManager(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.itemListOfSelectedCategories.collect { value ->
                if (binding.recyclerCategoryItems.adapter == null) {
                    itemsRecyclerAdapter =
                        ItemsRecyclerAdapter(
                            value,
                            viewModel,
                            this@CompanyFragment,
                            requireContext()
                        )
                    binding.recyclerCategoryItems.adapter = itemsRecyclerAdapter
                } else {
                    itemsRecyclerAdapter.updateData(value)
                    if (!binding.recyclerCategoryItems.isComputingLayout) {
                        itemsRecyclerAdapter.notifyDataSetChanged()
                    } else {
                        binding.recyclerCategoryItems.post {
                            itemsRecyclerAdapter.notifyDataSetChanged()
                        }
                    }
                }

                binding.lvCategoryItemLoading.visibility = View.GONE
                binding.recyclerCategoryItems.visibility = View.VISIBLE
            }
        }

        binding.btnCompleteOrder.setOnClickListener {
            if (viewModel.orderedItemsList.value.isEmpty()) {
                showAlertDialog(
                    requireContext(),
                    "You need to add value for ordered item",
                    {

                    },
                    { return@showAlertDialog })
            } else {
//                showAlertDialog(requireContext(), "Do you want to create a new order?", {
//                    completeOrder()
//                }, {
//                })
                val intent = Intent(requireActivity(), OrderedItemsActivity::class.java)
                intent.putParcelableArrayListExtra(
                    "ordered_item_list",
                    ArrayList(viewModel.orderedItemsList.value)
                )
                intent.putExtra(CUSTOMER_MODEL_KEY, selectedCustomer)
                intent.putExtra(COMPANY_TYPE_KEY, companyType)
                intent.putExtra(TOTAL_BILL, viewModel.totalBill.value)
                startActivity(intent)
            }
        }

        binding.btnCancelOrder.setOnClickListener {
            startActivity(Intent(requireContext(), CustomerSelectionActivity::class.java))
            requireActivity().finish()
        }

        binding.searchCategoryDropdown.setOnClickListener {
            binding.searchCategoryDropdown.showDropDown()
        }

        binding.searchCategoryDropdown.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                binding.searchCategoryDropdown.showDropDown()
            }
        }

        binding.searchCategoryDropdown.setOnItemClickListener { _, _, index, _ ->
            UtilityMethods.hideKeyBoard(binding.searchCategoryDropdown, requireContext())
            selectedCategory = categories[index]
            binding.lvCategoryItemLoading.visibility = View.VISIBLE
            viewModel.getItemsInSelectedCategory(selectedCategory)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.itemData.collect { data ->
                if (data.isNotEmpty()) {
                    itemList = data
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categories.collect { data ->
                if (data.isNotEmpty()) {
                    categories = data
                    try{
                        categories =
                            categories.sortedBy { it.substringBefore(" ").toInt() }.toMutableList()
                    }
                    catch (ex : Exception){
                        Log.e("ERROR OCCURRED", "$ex")
                    }

                    categoriesAdapter?.let {
                        it.clear()
                        it.addAll(categories)
                        it.notifyDataSetChanged()
                    }

                    viewModel.getItemsInSelectedCategory("")
                    binding.searchCategoryDropdown.isEnabled = true
                    binding.lvCategoryLoading.visibility = View.GONE
                    binding.clMainLayout.visibility = View.VISIBLE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.totalBill.collect { value ->
                if (value > 0F) {
                    binding.llBillAmount.visibility = View.VISIBLE
                    binding.llConfirmCancelOrder.visibility = View.VISIBLE
                    binding.tvTotalAmount.text =
                        BigDecimal(value.toString()).setScale(2, RoundingMode.HALF_UP)
                            .toFloat().toString()
                } else {
                    binding.llBillAmount.visibility = View.GONE
                    binding.llConfirmCancelOrder.visibility = View.GONE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.orderedItemsList.collect { value ->
//                if(value.isNotEmpty()){
//                    binding.llBillAmount.visibility = View.VISIBLE
//                    binding.llConfirmCancelOrder.visibility = View.VISIBLE
//                }
//                else{
//                    binding.llBillAmount.visibility = View.GONE
//                    binding.llConfirmCancelOrder.visibility = View.GONE
//                }
            }
        }

        billNumber.observe(viewLifecycleOwner) { value ->
            if (value > 0) {
                billId = UtilityMethods.getBillId(requireContext(), companyType)!!
                binding.tvBillNumber.text = "Bill No. : $billId - $value"
            }
        }
    }

    override fun onStart() {
        super.onStart()
        fetchItemList()

        if (UtilityMethods.getBillNumber(requireContext(), companyType) != 0L) {
            salesmanName = UtilityMethods.getSalesmanName(requireContext())
            billNumber.value = UtilityMethods.getBillNumber(requireContext(), companyType)
        } else {
            showBillDialog()
        }
    }

    private fun fetchItemList() {
        if (companyType == Constants.COMPANY_TYPE_ITC) {
            viewModel.fetchItemData(requireContext(),"https://drive.google.com/uc?export=download&id=${Constants.ITC_ITEM_FILE_DRIVE_ID}", companyType)
        } else {
            viewModel.fetchItemData(requireContext(),"https://drive.google.com/uc?export=download&id=${Constants.AVT_ITEM_FILE_DRIVE_ID}", companyType)
        }
    }

    override fun onItemValueChanged(currentItem: ItemsModel) {
        viewModel.addOrderItem(currentItem)
    }

    private fun showBillDialog() {
        billDialog = BillNumberDialog(this)
        billDialog?.isCancelable = false
        billDialog?.show(childFragmentManager, "bill_dialog")
    }

    override fun onSubmitButtonTap(billId: String, billNumber: Long) {
        billDialog?.dismiss()
        UtilityMethods.setBillNumber(requireContext(), billNumber, billId, companyType)
        this.billId = billId
        this.billNumber.value = billNumber
    }
}