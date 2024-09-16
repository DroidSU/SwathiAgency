package com.sujoy.swathiagency.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sujoy.swathiagency.R
import com.sujoy.swathiagency.data.datamodels.CustomerOrderModel
import java.math.BigDecimal
import java.math.RoundingMode

class CustomerOrderRecyclerAdapter(private val customerOrderList: MutableList<CustomerOrderModel>) :
    RecyclerView.Adapter<CustomerOrderRecyclerAdapter.CustomerOrderViewHolder>() {

    inner class CustomerOrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrderDate: TextView = itemView.findViewById(R.id.tv_customer_order_date)
        val tvCustomerName: TextView = itemView.findViewById(R.id.tv_customer_order_name)
        val tvTotalValue: TextView = itemView.findViewById(R.id.tv_customer_order_total)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerOrderViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_order_view_recycler_item, parent, false)
        return CustomerOrderViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return customerOrderList.size
    }

    override fun onBindViewHolder(holder: CustomerOrderViewHolder, position: Int) {
        val currentItem = customerOrderList[position]

        holder.tvOrderDate.text = currentItem.date
        holder.tvCustomerName.text = currentItem.customerName
        holder.tvTotalValue.text = BigDecimal(currentItem.orderTotal.toString()).setScale(2, RoundingMode.HALF_UP).toFloat()
            .toString()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList : MutableList<CustomerOrderModel>) {
        customerOrderList.clear()
        customerOrderList.addAll(newList)
        notifyDataSetChanged()
    }
}