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
import android.widget.Toast
import com.example.android.autofillframework.R
import kotlinx.android.synthetic.main.virtual_login_activity.clear
import kotlinx.android.synthetic.main.virtual_login_activity.custom_view
import kotlinx.android.synthetic.main.virtual_login_activity.login


class VirtualSignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.virtual_login_activity)

        login.setOnClickListener { submitLogin() }
        clear.setOnClickListener { resetFields() }
    }

    private fun resetFields() {
        custom_view.resetFields()
    }

    /**
     * Emulates a login action.
     */
    private fun submitLogin() {
        val username = custom_view.usernameText.toString()
        val password = custom_view.passwordText.toString()
        val valid = isValidCredentials(username, password)
        if (valid) {
            val intent = WelcomeActivity.getStartActivityIntent(this@VirtualSignInActivity)
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

    companion object {

        fun getStartActivityIntent(context: Context): Intent {
            val intent = Intent(context, VirtualSignInActivity::class.java)
            return intent
        }
    }
}
