package com.sujoy.swathiagency.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sujoy.swathiagency.data.CompanyType
import com.sujoy.swathiagency.data.CustomerModel
import com.sujoy.swathiagency.ui.ITCFragment

class CompanyViewPagerAdapter(fragment: FragmentActivity, private var selectedCustomer : CustomerModel) : FragmentStateAdapter(fragment) {

    // Number of pages in the ViewPager
    override fun getItemCount(): Int {
        return 2
    }

    // Returns a new fragment for the given page
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ITCFragment.newInstance(customerModel = selectedCustomer, companyType = CompanyType.ITC)
            1 -> ITCFragment.newInstance(customerModel = selectedCustomer, companyType = CompanyType.AVT)
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}