package com.sujoy.swathiagency.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sujoy.swathiagency.R
import com.sujoy.swathiagency.data.datamodels.CustomerModel
import com.sujoy.swathiagency.interfaces.OnRecyclerItemClickedListener

class CustomersRecyclerAdapter(
    var context: Context,
    private var customerList: List<CustomerModel>,
    private var onRecyclerItemClickedListener: OnRecyclerItemClickedListener
) :
    RecyclerView.Adapter<CustomersRecyclerAdapter.CustomersViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    inner class CustomersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCustomerName: TextView = itemView.findViewById(R.id.tv_customer_name)
        val tvCustomerAmount: TextView = itemView.findViewById(R.id.tv_customer_pending_amount)
        val tvCustomerRoute : TextView = itemView.findViewById(R.id.tv_customer_route)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomersViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_customer_recycler_item, parent, false)
        return CustomersViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return customerList.size - 1
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<CustomerModel>) {
        customerList = newItems
        selectedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: CustomersViewHolder, position: Int) {
        val currentPosition = holder.adapterPosition
        val customer = customerList[currentPosition]

        holder.tvCustomerName.text = customer.customerName
        holder.tvCustomerAmount.text = customer.customerAmount.toString()
        holder.tvCustomerRoute.text = customer.customerRoute

        if (currentPosition == selectedPosition) {
            holder.itemView.background =
                ContextCompat.getDrawable(context, R.drawable.rounded_corner_black_border)
            holder.itemView.elevation = 10F
        } else {
            holder.itemView.background =
                ContextCompat.getDrawable(context, R.drawable.rounded_corner_no_border)
            holder.itemView.elevation = 0F
        }

        holder.itemView.setOnClickListener {
            onRecyclerItemClickedListener.onCustomerClicked(customer)
            val lastSelectedPos = selectedPosition
            selectedPosition = currentPosition
            notifyItemChanged(selectedPosition)
            notifyItemChanged(lastSelectedPos)
        }
    }
}