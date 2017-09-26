/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.autofillframework.app

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.autofill.AutofillManager
import android.view.autofill.AutofillValue
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import com.example.android.autofillframework.R
import kotlinx.android.synthetic.main.cc_exp_date.view.ccExpMonth
import kotlinx.android.synthetic.main.cc_exp_date.view.ccExpYear
import java.util.Calendar

class CreditCardExpirationDateView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val startYear = Calendar.getInstance().get(Calendar.YEAR)

    init {
        LayoutInflater.from(context).inflate(R.layout.cc_exp_date, this)
        importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_YES_EXCLUDE_DESCENDANTS
        val spinnerOnItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int,
                    id: Long) {
                context.getSystemService(AutofillManager::class.java)
                        .notifyValueChanged(this@CreditCardExpirationDateView)
            }

            override fun onNothingSelected(parent: AdapterView<*>) = Unit
        }
        val years = arrayOfNulls<String>(CC_EXP_YEARS_COUNT)
        for (i in 0 until years.size) {
            years[i] = Integer.toString(startYear + i)
        }

        with(ccExpMonth) {
            adapter = ArrayAdapter.createFromResource(context, R.array.month_array,
                    android.R.layout.simple_spinner_item).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            onItemSelectedListener = spinnerOnItemSelectedListener
        }

        with(ccExpYear) {
            adapter = ArrayAdapter<String>(context,
                    android.R.layout.simple_spinner_item, years)
            onItemSelectedListener = spinnerOnItemSelectedListener
        }
    }

    override fun getAutofillValue() =
            AutofillValue.forDate(Calendar.getInstance().apply {
                // clear() sets hours, minutes, seconds, and millis to 0 to ensure that after
                // autofill() is called, getAutofillValue() == the value that was originally passed
                // into autofill(). Without clear(), the view will not turn yellow when updated.
                clear()
                val year = Integer.parseInt(ccExpYear.selectedItem.toString())
                val month = ccExpMonth.selectedItemPosition
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
            }.timeInMillis)

    override fun autofill(value: AutofillValue) {
        if (!value.isDate) {
            return
        }
        val calendar = Calendar.getInstance().apply {
            timeInMillis = value.dateValue
        }
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        ccExpMonth.setSelection(month)
        ccExpYear.setSelection(year - startYear)
    }

    override fun getAutofillType() = View.AUTOFILL_TYPE_DATE

    fun reset() {
        ccExpMonth.setSelection(0)
        ccExpYear.setSelection(0)
    }

    companion object {
        private const val CC_EXP_YEARS_COUNT = 5
    }
}