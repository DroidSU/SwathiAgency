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
import com.sujoy.swathiagency.data.datamodels.ITCItemsModel
import com.sujoy.swathiagency.interfaces.OnItemEvent
import com.sujoy.swathiagency.utilities.UtilityMethods
import com.sujoy.swathiagency.viewmodels.CompanyViewModel
import java.math.BigDecimal
import java.math.RoundingMode

class ItemsRecyclerAdapter(
    private var itemList: MutableList<ITCItemsModel>,
    private var itcViewModel: CompanyViewModel,
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
//        val currentItem =
//        val itemInPos = itcViewModel.itemData.value.indexOf(itemInCategoryList)
//        val currentItem = itcViewModel.itemData.value[itemInPos]

        var currentItem = itemList[position]

        holder.tvItemName.text = currentItem.itemName
        holder.textViewAvailable.text = currentItem.availableQuantity
        if(currentItem.availableQuantity.toInt() < 0)
            holder.textViewAvailable.setTextColor(context.getColor(android.R.color.holo_red_light))
        holder.textViewMRP.text = currentItem.mrp

        holder.mainConstraintLayout.setOnClickListener {
//            currentItem.selected = !currentItem.selected

            if (currentItem.selected && (currentItem.numberOfBoxesOrdered == 0 && currentItem.numberOfPcsOrdered == 0)) {
                currentItem.selected = false
            } else {
                currentItem.selected = true
            }

            setDataOnViews(holder, currentItem)
        }

        holder.boxIncrementButton.setOnClickListener {
            if (holder.boxCountContainer.isEnabled) {
                currentItem.numberOfBoxesOrdered += 1
                holder.boxCountEditText.setText(currentItem.numberOfBoxesOrdered.toString())
            }
        }

        holder.boxDecrementButton.setOnClickListener {
            if (holder.boxCountContainer.isEnabled && currentItem.numberOfBoxesOrdered > 0) {
                currentItem.numberOfBoxesOrdered -= 1
                holder.boxCountEditText.setText(currentItem.numberOfBoxesOrdered.toString())
            }
        }

        holder.pcsIncrementButton.setOnClickListener {
            if (holder.pcsCountContainer.isEnabled) {
                currentItem.numberOfPcsOrdered += 1
                holder.pcsCountEditText.setText(currentItem.numberOfPcsOrdered.toString())
            }
        }

        holder.pcsDecrementButton.setOnClickListener {
            if (holder.pcsCountContainer.isEnabled && currentItem.numberOfPcsOrdered > 0) {
                currentItem.numberOfPcsOrdered -= 1
                holder.pcsCountEditText.setText(currentItem.numberOfPcsOrdered.toString())
            }
        }

        setDataOnViews(holder, currentItem)
    }

    private fun setDataOnViews(holder: ItemsViewHolder, currentItem: ITCItemsModel) {
        // Store reference to listeners
        val boxTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

                if (!s.isNullOrEmpty()) {
                    currentItem.numberOfBoxesOrdered = s.toString().toInt()
                } else {
                    currentItem.numberOfBoxesOrdered = 0
                }

                updateTotalValue(holder, currentItem)
                onItemEvent.onItemValueChanged(currentItem)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        val pcsTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    currentItem.numberOfPcsOrdered = s.toString().toInt()
                } else {
                    currentItem.numberOfPcsOrdered = 0
                }

                updateTotalValue(holder, currentItem)
                onItemEvent.onItemValueChanged(currentItem)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        if (currentItem.selected) {
            holder.boxCountContainer.visibility = View.VISIBLE
            holder.pcsCountContainer.visibility = View.VISIBLE
            holder.totalAmountContainer.visibility = View.VISIBLE
            holder.textViewTotalValue.text = "${currentItem.totalAmount}"

            // Detach the listener before setting the text
            holder.boxCountEditText.removeTextChangedListener(boxTextWatcher)
            holder.pcsCountEditText.removeTextChangedListener(pcsTextWatcher)

            if(currentItem.numberOfBoxesOrdered > 0)
                holder.boxCountEditText.setText(currentItem.numberOfBoxesOrdered.toString())
            else{
                holder.boxCountEditText.setText("")
                holder.boxCountEditText.hint = "0"
            }

            if(currentItem.numberOfPcsOrdered > 0)
                holder.pcsCountEditText.setText(currentItem.numberOfPcsOrdered.toString())
            else{
                holder.pcsCountEditText.setText("")
                holder.pcsCountEditText.hint = "0"
            }

            holder.boxCountEditText.addTextChangedListener(boxTextWatcher)
            holder.pcsCountEditText.addTextChangedListener(pcsTextWatcher)

        } else {
            holder.boxCountContainer.visibility = View.GONE
            holder.pcsCountContainer.visibility = View.GONE
            holder.totalAmountContainer.visibility = View.GONE
        }
    }

    private fun updateTotalValue(
        holder: ItemsViewHolder,
        currentItem: ITCItemsModel
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
        newItems: MutableList<ITCItemsModel>
    ) {
        itemList.clear()
        itemList.addAll(newItems)
        notifyDataSetChanged()
    }
}