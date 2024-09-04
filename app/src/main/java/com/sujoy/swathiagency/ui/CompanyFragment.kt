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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sujoy.swathiagency.adapters.ItemsRecyclerAdapter
import com.sujoy.swathiagency.data.datamodels.CompanyType
import com.sujoy.swathiagency.data.datamodels.CustomerModel
import com.sujoy.swathiagency.data.datamodels.ITCItemsModel
import com.sujoy.swathiagency.databinding.FragmentItcBinding
import com.sujoy.swathiagency.interfaces.OnItemEvent
import com.sujoy.swathiagency.network.NetworkRepository
import com.sujoy.swathiagency.utilities.Constants
import com.sujoy.swathiagency.utilities.UtilityMethods
import com.sujoy.swathiagency.utilities.UtilityMethods.Companion.showAlertDialog
import com.sujoy.swathiagency.viewmodels.ITCVMFactory
import com.sujoy.swathiagency.viewmodels.ITCViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

class CompanyFragment : Fragment(), OnItemEvent {

    companion object {
        private const val CUSTOMER_MODEL_KEY = "CUSTOMER_MODEL"
        private const val COMPANY_TYPE_KEY = "COMPANY_TYPE"

        fun newInstance(companyType: CompanyType, customerModel: CustomerModel): CompanyFragment {
            val fragment = CompanyFragment()
            val args = Bundle()
            args.putSerializable(COMPANY_TYPE_KEY, companyType)
            args.putParcelable(CUSTOMER_MODEL_KEY, customerModel)
            fragment.arguments = args
            return fragment
        }
    }

    private val viewModel: ITCViewModel by viewModels {
        ITCVMFactory(NetworkRepository(requireContext()))
    }

    private lateinit var binding: FragmentItcBinding

    private lateinit var selectedCustomer: CustomerModel
    private var categories: MutableList<String> = mutableListOf()
    private var categoriesAdapter: ArrayAdapter<String>? = null
    private var itemList: ArrayList<ITCItemsModel> = arrayListOf()
    private var selectedCategory: String = ""
    private var totalBillAmount: Float = 0F

    private lateinit var itemsRecyclerAdapter: ItemsRecyclerAdapter
    private lateinit var lottieOverlayFragment: LottieOverlayFragment

    private lateinit var companyType: CompanyType

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
        companyType = (arguments?.getSerializable(COMPANY_TYPE_KEY) as CompanyType?)!!

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
                intent.putExtra("customer_model", selectedCustomer)
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
    }

    override fun onStart() {
        super.onStart()
        if (UtilityMethods.isNetworkAvailable(requireContext())) {
            if (companyType == CompanyType.ITC) {
                viewModel.fetchItemData("https://drive.google.com/uc?export=download&id=${Constants.ITC_ITEM_FILE_DRIVE_ID}")
            } else {
                viewModel.fetchItemData("https://drive.google.com/uc?export=download&id=${Constants.AVT_ITEM_FILE_DRIVE_ID}")
            }
        } else {
            Toast.makeText(
                requireContext(),
                "No internet connection available!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onItemValueChanged(currentItem: ITCItemsModel) {
        viewModel.addOrderItem(currentItem)
    }
}