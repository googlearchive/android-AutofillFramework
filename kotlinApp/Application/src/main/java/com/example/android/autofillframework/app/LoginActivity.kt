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
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import com.example.android.autofillframework.R

class LoginActivity : AppCompatActivity() {

    private var mUsernameEditText: EditText? = null
    private var mPasswordEditText: EditText? = null
    private var mLoginButton: Button? = null
    private var mClearButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.login_activity)

        mLoginButton = findViewById(R.id.login) as Button
        mClearButton = findViewById(R.id.clear) as Button
        mUsernameEditText = findViewById(R.id.usernameField) as EditText
        mPasswordEditText = findViewById(R.id.passwordField) as EditText
        mLoginButton!!.setOnClickListener { login() }
        mClearButton!!.setOnClickListener { resetFields() }
    }

    private fun resetFields() {
        mUsernameEditText!!.setText("")
        mPasswordEditText!!.setText("")
    }

    /**
     * Emulates a login action.
     */
    private fun login() {
        val username = mUsernameEditText!!.text.toString()
        val password = mPasswordEditText!!.text.toString()
        val valid = isValidCredentials(username, password)
        if (valid) {
            val intent = WelcomeActivity.getStartActivityIntent(this@LoginActivity)
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
            val intent = Intent(context, LoginActivity::class.java)
            return intent
        }
    }
}