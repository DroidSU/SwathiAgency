package com.sujoy.swathiagency.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sujoy.swathiagency.data.datamodels.CustomerModel
import com.sujoy.swathiagency.data.datamodels.ItemsModel
import com.sujoy.swathiagency.ui.CompanyFragment
import com.sujoy.swathiagency.utilities.Constants

class CompanyViewPagerAdapter(
    fragment: FragmentActivity,
    private var selectedCustomer: CustomerModel,
    private var itcOrderList: MutableList<ItemsModel>,
    private var avtOrderList: MutableList<ItemsModel>,
    private val orderid : String
) : FragmentStateAdapter(fragment) {

    // Number of pages in the ViewPager
    override fun getItemCount(): Int {
        return 2
    }

    // Returns a new fragment for the given page
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CompanyFragment.newInstance(
                customerModel = selectedCustomer,
                companyType = Constants.COMPANY_TYPE_ITC,
                itemModelList = ArrayList(itcOrderList),
                orderId = orderid
            )

            1 -> CompanyFragment.newInstance(
                customerModel = selectedCustomer,
                companyType = Constants.COMPANY_TYPE_AVT,
                itemModelList = ArrayList(avtOrderList),
                orderId = orderid
            )

            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}