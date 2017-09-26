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
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import com.example.android.autofillframework.R
import kotlinx.android.synthetic.main.credit_card_activity.clear
import kotlinx.android.synthetic.main.credit_card_activity.creditCardNumberField
import kotlinx.android.synthetic.main.credit_card_activity.expirationDay
import kotlinx.android.synthetic.main.credit_card_activity.expirationMonth
import kotlinx.android.synthetic.main.credit_card_activity.expirationYear
import kotlinx.android.synthetic.main.credit_card_activity.submit
import java.util.Calendar


class CreditCardActivity : AppCompatActivity() {

    private val CC_EXP_YEARS_COUNT = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.credit_card_activity)

        // Create an ArrayAdapter using the string array and a default spinner layout
        expirationDay.adapter = ArrayAdapter.createFromResource(this, R.array.day_array,
                android.R.layout.simple_spinner_item).apply {
            // Specify the layout to use when the list of choices appears
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        expirationMonth.adapter = ArrayAdapter.createFromResource(this, R.array.month_array,
                android.R.layout.simple_spinner_item).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val year = Calendar.getInstance().get(Calendar.YEAR)

        val years = (0 until CC_EXP_YEARS_COUNT)
                .map { Integer.toString(year + it) }
                .toTypedArray<CharSequence>()

        expirationYear.adapter = object : ArrayAdapter<CharSequence?>(this,
                android.R.layout.simple_spinner_item, years) {
            override fun getAutofillOptions() = years
        }
        submit.setOnClickListener { submitCcInfo() }
        clear.setOnClickListener { resetFields() }
    }

    private fun resetFields() {
        creditCardNumberField.setText("")
        expirationDay.setSelection(0)
        expirationMonth.setSelection(0)
        expirationYear.setSelection(0)
    }

    /**
     * Launches new Activity and finishes, triggering an autofill save request if the user entered
     * any new data.
     */
    private fun submitCcInfo() {
        startActivity(WelcomeActivity.getStartActivityIntent(this))
        finish()
    }

    companion object {
        fun getStartActivityIntent(context: Context) =
                Intent(context, CreditCardActivity::class.java)
    }
}
