package com.sujoy.swathiagency.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.sujoy.swathiagency.R
import com.sujoy.swathiagency.data.datamodels.ItemsModel
import com.sujoy.swathiagency.interfaces.OnItemEvent
import com.sujoy.swathiagency.utilities.UtilityMethods
import java.math.BigDecimal
import java.math.RoundingMode

class ItemsRecyclerAdapter(
    private var itemList: MutableList<ItemsModel>,
    private val onItemEvent: OnItemEvent,
    private val context: Context
) :
    RecyclerView.Adapter<ItemsRecyclerAdapter.ItemsViewHolder>() {

    inner class ItemsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvItemName: TextView = itemView.findViewById(R.id.item_name)
        val boxIncrementButton: Button = itemView.findViewById(R.id.btn_increment_box)
        val boxDecrementButton: Button = itemView.findViewById(R.id.btn_decrement_box)
        val pcsIncrementButton: Button = itemView.findViewById(R.id.btn_increment_pcs)
        val pcsDecrementButton: Button = itemView.findViewById(R.id.btn_decrement_pcs)
        val boxCountEditText: EditText = itemView.findViewById(R.id.et_count_value_box)
        val pcsCountEditText: EditText = itemView.findViewById(R.id.et_count_value_pcs)
        val boxCountContainer: LinearLayout = itemView.findViewById(R.id.ll_box_count)
        val pcsCountContainer: LinearLayout = itemView.findViewById(R.id.ll_pcs_count)
        val textViewMRP: TextView = itemView.findViewById(R.id.tv_item_mrp)
        val textViewAvailable: TextView = itemView.findViewById(R.id.tv_available_count)
        val textViewTotalValue: TextView = itemView.findViewById(R.id.tv_item_total_amount)
        val totalAmountContainer: LinearLayout = itemView.findViewById(R.id.ll_item_total_amount)
        val mainConstraintLayout: ConstraintLayout = itemView.findViewById(R.id.cl_item_main)

        var boxTextWatcher : TextWatcher? = null
        var pcsTextWatcher : TextWatcher? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_category_items, parent, false)
        return ItemsViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemsViewHolder, position: Int) {
        val currentPosition = holder.adapterPosition
        val currentItem = itemList[currentPosition]

        // Detach the listener before setting the text
        holder.boxCountEditText.removeTextChangedListener(holder.boxTextWatcher)
        holder.pcsCountEditText.removeTextChangedListener(holder.pcsTextWatcher)

        holder.tvItemName.text = currentItem.itemName
        holder.textViewAvailable.text = currentItem.availableQuantity
        holder.textViewMRP.text = currentItem.mrp

        if (currentItem.availableQuantity.toFloatOrNull() == null || currentItem.availableQuantity.toFloat() < 0F)
            holder.textViewAvailable.setTextColor(context.getColor(android.R.color.holo_red_light))

        if (currentItem.selected) {
            holder.boxCountContainer.visibility = View.VISIBLE
            holder.pcsCountContainer.visibility = View.VISIBLE
            holder.totalAmountContainer.visibility = View.VISIBLE
            holder.textViewTotalValue.text = "${currentItem.totalAmount}"

            if (currentItem.numberOfBoxesOrdered > 0)
                holder.boxCountEditText.setText(currentItem.numberOfBoxesOrdered.toString())
            else {
                holder.boxCountEditText.setText("")
                holder.boxCountEditText.hint = "0"
            }

            if (currentItem.numberOfPcsOrdered > 0)
                holder.pcsCountEditText.setText(currentItem.numberOfPcsOrdered.toString())
            else {
                holder.pcsCountEditText.setText("")
                holder.pcsCountEditText.hint = "0"
            }
        } else {
            holder.boxCountContainer.visibility = View.GONE
            holder.pcsCountContainer.visibility = View.GONE
            holder.totalAmountContainer.visibility = View.GONE
        }

        val boxTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

                if (!s.isNullOrEmpty()) {
                    currentItem.numberOfBoxesOrdered = s.toString().toInt()
                } else {
                    currentItem.numberOfBoxesOrdered = 0
                }

                onItemEvent.onItemValueChanged(currentItem)
                updateTotalValue(holder, currentItem)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        holder.boxCountEditText.addTextChangedListener(boxTextWatcher)
        holder.boxTextWatcher = boxTextWatcher

        val pcsTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    currentItem.numberOfPcsOrdered = s.toString().toInt()
                } else {
                    currentItem.numberOfPcsOrdered = 0
                }

                onItemEvent.onItemValueChanged(currentItem)
                updateTotalValue(holder, currentItem)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        holder.pcsCountEditText.addTextChangedListener(pcsTextWatcher)
        holder.pcsTextWatcher = pcsTextWatcher

        holder.mainConstraintLayout.setOnClickListener {
            UtilityMethods.hideKeyBoard(holder.mainConstraintLayout, context)
            currentItem.selected =
                !(currentItem.selected && (currentItem.numberOfBoxesOrdered == 0 && currentItem.numberOfPcsOrdered == 0))
            notifyItemChanged(currentPosition)
        }

        holder.boxIncrementButton.setOnClickListener {
            if (currentItem.selected) {
                currentItem.numberOfBoxesOrdered += 1
                notifyItemChanged(currentPosition)
            }
        }

        holder.boxDecrementButton.setOnClickListener {
            if (currentItem.selected && currentItem.numberOfBoxesOrdered > 0) {
                currentItem.numberOfBoxesOrdered -= 1
                notifyItemChanged(currentPosition)
            }
        }

        holder.pcsIncrementButton.setOnClickListener {
            if (currentItem.selected) {
                currentItem.numberOfPcsOrdered += 1
                notifyItemChanged(currentPosition)
            }
        }

        holder.pcsDecrementButton.setOnClickListener {
            if (currentItem.selected && currentItem.numberOfPcsOrdered > 0) {
                currentItem.numberOfPcsOrdered -= 1
                notifyItemChanged(currentPosition)
            }
        }

        onItemEvent.onItemValueChanged(currentItem)
        updateTotalValue(holder, currentItem)
    }

    private fun updateTotalValue(
        holder: ItemsViewHolder,
        currentItem: ItemsModel
    ) {
        UtilityMethods.calculateItemTotalValue(currentItem)
        holder.textViewTotalValue.text =
            BigDecimal(currentItem.totalAmount.toString()).setScale(2, RoundingMode.HALF_UP)
                .toFloat().toString()
        if (currentItem.totalAmount > 0)
            holder.totalAmountContainer.visibility = View.VISIBLE
        else
            holder.totalAmountContainer.visibility = View.GONE
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateData(
        newItems: MutableList<ItemsModel>
    ) {
        itemList.clear()
        itemList.addAll(newItems)
        notifyDataSetChanged()
    }
}