package com.sujoy.swathiagency.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.withStyledAttributes
import com.sujoy.swathiagency.R

class CounterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defaultValue : Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    private var currentValue = 0
    private lateinit var textViewCounter: TextView
    private lateinit var buttonIncrement: Button
    private lateinit var buttonDecrement: Button

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_counter_view, this, true)

        textViewCounter = findViewById(R.id.tv_counter_value)
        buttonIncrement = findViewById(R.id.btn_increment)
        buttonDecrement = findViewById(R.id.btn_decrement)

        context.withStyledAttributes(attrs, R.styleable.CounterView) {
            currentValue = getInt(R.styleable.CounterView_startValue, 0)  // Default value is 0 if not set
            textViewCounter.text = currentValue.toString()
        }

        buttonIncrement.setOnClickListener {
            currentValue++
            textViewCounter.text = currentValue.toString()
        }

        buttonDecrement.setOnClickListener {
            if (currentValue > 0) {
                currentValue--
                textViewCounter.text = currentValue.toString()
            }
        }
    }

    fun getValue(): Int = currentValue

    private fun setValue(value: Int) {
        currentValue = value
        textViewCounter.text = currentValue.toString()
    }
}