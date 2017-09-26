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
import com.example.android.autofillframework.R
import kotlinx.android.synthetic.main.credit_card_compound_view_activity.clearButton
import kotlinx.android.synthetic.main.credit_card_compound_view_activity.creditCardExpirationView
import kotlinx.android.synthetic.main.credit_card_compound_view_activity.creditCardNumberField
import kotlinx.android.synthetic.main.credit_card_compound_view_activity.submitButton

class CreditCardCompoundViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.credit_card_compound_view_activity)
        submitButton.setOnClickListener { submit() }
        clearButton.setOnClickListener { resetFields() }
    }

    private fun resetFields() {
        creditCardExpirationView.reset()
        creditCardNumberField.setText("")
    }

    /**
     * Launches new Activity and finishes, triggering an autofill save request if the user entered
     * any new data.
     */
    private fun submit() {
        val intent = WelcomeActivity.getStartActivityIntent(this)
        startActivity(intent)
        finish()
    }

    companion object {
        fun getStartActivityIntent(context: Context): Intent {
            val intent = Intent(context, CreditCardCompoundViewActivity::class.java)
            return intent
        }
    }
}
