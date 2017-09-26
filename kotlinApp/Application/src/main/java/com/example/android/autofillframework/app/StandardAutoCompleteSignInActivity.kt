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
import android.util.Log
import android.view.View
import android.view.autofill.AutofillManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import com.example.android.autofillframework.CommonUtil.TAG
import com.example.android.autofillframework.R
import kotlinx.android.synthetic.main.login_with_autocomplete_activity.clear
import kotlinx.android.synthetic.main.login_with_autocomplete_activity.login
import kotlinx.android.synthetic.main.login_with_autocomplete_activity.passwordField
import kotlinx.android.synthetic.main.login_with_autocomplete_activity.usernameField

class StandardAutoCompleteSignInActivity : AppCompatActivity() {
    private var autofillReceived = false
    private lateinit var autofillCallback: AutofillManager.AutofillCallback
    private lateinit var autofillManager: AutofillManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.login_with_autocomplete_activity)

        login.setOnClickListener { submitLogin() }
        clear.setOnClickListener { resetFields() }
        autofillCallback = MyAutofillCallback()
        autofillManager = getSystemService(AutofillManager::class.java)
        val mockAutocompleteAdapter = ArrayAdapter.createFromResource(this, R.array.mock_autocomplete_sign_in_suggestions,
                android.R.layout.simple_dropdown_item_1line)
        usernameField.setAdapter(mockAutocompleteAdapter)
    }

    override fun onResume() {
        super.onResume()
        autofillManager.registerCallback(autofillCallback)
    }

    override fun onPause() {
        super.onPause()
        autofillManager.unregisterCallback(autofillCallback)
    }

    private fun resetFields() {
        usernameField.setText("")
        passwordField.setText("")
    }

    /**
     * Emulates a login action.
     */
    private fun submitLogin() {
        val username = usernameField.text.toString()
        val password = passwordField.text.toString()
        val valid = isValidCredentials(username, password)
        if (valid) {
            val intent = WelcomeActivity.getStartActivityIntent(this@StandardAutoCompleteSignInActivity)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Dummy implementation for demo purposes. A real service should use secure mechanisms to
     * authenticate users.
     */
    fun isValidCredentials(username: String?, password: String?): Boolean {
        return username != null && password != null && username == password
    }

    private inner class MyAutofillCallback : AutofillManager.AutofillCallback() {
        override fun onAutofillEvent(view: View, event: Int) {
            super.onAutofillEvent(view, event)
            if (view is AutoCompleteTextView) {
                when (event) {
                    AutofillManager.AutofillCallback.EVENT_INPUT_UNAVAILABLE,
                    AutofillManager.AutofillCallback.EVENT_INPUT_HIDDEN -> {
                        if (!autofillReceived) {
                            view.showDropDown()
                        }
                    }
                    AutofillManager.AutofillCallback.EVENT_INPUT_SHOWN -> {
                        autofillReceived = true
                        view.setAdapter(null)
                    }
                    else -> Log.d(TAG, "Unexpected callback: " + event)
                }
            }
        }
    }

    companion object {

        fun getStartActivityIntent(context: Context): Intent {
            val intent = Intent(context, StandardAutoCompleteSignInActivity::class.java)
            return intent
        }
    }
}