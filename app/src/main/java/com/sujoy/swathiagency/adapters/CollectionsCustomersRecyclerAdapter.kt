package com.sujoy.swathiagency.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sujoy.swathiagency.R
import com.sujoy.swathiagency.data.datamodels.CollectionsCustomerModel
import com.sujoy.swathiagency.interfaces.OnCashCollectionRecyclerItemClick

class CollectionsCustomersRecyclerAdapter(
    private val context: Context,
    private val collectionsCustomerList: MutableList<CollectionsCustomerModel>,
    private var onRecyclerItemClickedListener: OnCashCollectionRecyclerItemClick
) :
    RecyclerView.Adapter<CollectionsCustomersRecyclerAdapter.CollectionsCustomersViewHolder>() {

    inner class CollectionsCustomersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvInvoiceNumber = itemView.findViewById<TextView>(R.id.tv_invoice_number)
        val tvInvoiceDate = itemView.findViewById<TextView>(R.id.tv_invoice_date)
        val tvCustomerName = itemView.findViewById<TextView>(R.id.tv_customer_name)
        val tvInvoiceAmount = itemView.findViewById<TextView>(R.id.tv_invoice_amount)
        val tvReceivedAmount = itemView.findViewById<TextView>(R.id.tv_rcvd_amount)
        val tvDueAmount = itemView.findViewById<TextView>(R.id.tv_due_amount)
        val tvDueDate = itemView.findViewById<TextView>(R.id.tv_due_date)

        init {
            itemView.setOnClickListener {
                onRecyclerItemClickedListener.onItemClicked(collectionsCustomerList[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CollectionsCustomersViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_collection_adapter_item, parent, false)
        return CollectionsCustomersViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return collectionsCustomerList.size
    }

    override fun onBindViewHolder(holder: CollectionsCustomersViewHolder, position: Int) {
        val currentItem = collectionsCustomerList[holder.adapterPosition]
        holder.tvInvoiceNumber.text = currentItem.invoiceNumber
        holder.tvInvoiceDate.text = currentItem.invoiceDate
        holder.tvCustomerName.text = currentItem.customerName
        holder.tvInvoiceAmount.text = currentItem.invoiceAmount.toString()
        holder.tvReceivedAmount.text = currentItem.received.toString()
        holder.tvDueAmount.text = currentItem.dueAmount.toString()
        holder.tvDueDate.text = currentItem.dueDays.toString()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: MutableList<CollectionsCustomerModel>) {
        collectionsCustomerList.clear()
        collectionsCustomerList.addAll(newList)
        notifyDataSetChanged()
    }

    fun updateItem(item : CollectionsCustomerModel){
        val index = collectionsCustomerList.indexOfFirst { it.customerId == item.customerId }
        collectionsCustomerList[index] = item
        notifyItemChanged(index)
    }
}