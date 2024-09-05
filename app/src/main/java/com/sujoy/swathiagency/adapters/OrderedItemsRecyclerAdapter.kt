package com.sujoy.swathiagency.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sujoy.swathiagency.R
import com.sujoy.swathiagency.data.datamodels.ITCItemsModel
import java.math.BigDecimal
import java.math.RoundingMode

class OrderedItemsRecyclerAdapter(private var orderedItemList: MutableList<ITCItemsModel>) :
    RecyclerView.Adapter<OrderedItemsRecyclerAdapter.OrderedItemsViewHolder>() {

    inner class OrderedItemsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewItemTotal: TextView = itemView.findViewById(R.id.tv_ordered_item_total)
        val textViewItemName: TextView = itemView.findViewById(R.id.tv_ordered_item_name)
        val textViewItemBoxCount: TextView = itemView.findViewById(R.id.tv_ordered_item_boxes)
        val textViewItemPcsCount: TextView = itemView.findViewById(R.id.tv_ordered_item_pcs)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderedItemsViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_ordered_item, parent, false)
        return OrderedItemsViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return orderedItemList.size
    }

    override fun onBindViewHolder(holder: OrderedItemsViewHolder, position: Int) {
        val currentItem = orderedItemList[position]

        holder.textViewItemTotal.text =
            BigDecimal(currentItem.totalAmount.toString()).setScale(2, RoundingMode.HALF_UP)
                .toFloat().toString()
        holder.textViewItemName.text = currentItem.itemName
        holder.textViewItemBoxCount.text = currentItem.numberOfBoxesOrdered.toString()
        holder.textViewItemPcsCount.text = currentItem.numberOfPcsOrdered.toString()
    }
}