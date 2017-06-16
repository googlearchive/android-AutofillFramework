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

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.example.android.autofillframework.R
import kotlinx.android.synthetic.main.activity_main.creditCardCheckoutButton
import kotlinx.android.synthetic.main.activity_main.emailComposeButton
import kotlinx.android.synthetic.main.activity_main.standardLoginWithAutoCompleteButton
import kotlinx.android.synthetic.main.activity_main.standardViewSignInButton
import kotlinx.android.synthetic.main.activity_main.virtualViewSignInButton

/**
 * This is used to launch sample activities that showcase autofill.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        standardViewSignInButton.setNavigationButtonClickListener(View.OnClickListener { standardViewSignIn() })
        virtualViewSignInButton.setNavigationButtonClickListener(View.OnClickListener { virtualViewSignIn() })
        creditCardCheckoutButton.setNavigationButtonClickListener(View.OnClickListener { creditCardCheckout() })
        standardLoginWithAutoCompleteButton.setNavigationButtonClickListener(View.OnClickListener { standardAutoCompleteSignIn() })
        emailComposeButton.setNavigationButtonClickListener(View.OnClickListener { emailCompose() })
    }

    private fun creditCardCheckout() {
        val intent = CreditCardActivity.getStartActivityIntent(this)
        startActivity(intent)
    }

    private fun standardViewSignIn() {
        val intent = StandardSignInActivity.getStartActivityIntent(this)
        startActivity(intent)
    }

    private fun standardAutoCompleteSignIn() {
        val intent = StandardAutoCompleteSignInActivity.getStartActivityIntent(this)
        startActivity(intent)
    }

    private fun virtualViewSignIn() {
        val intent = VirtualSignInActivity.getStartActivityIntent(this)
        startActivity(intent)
    }

    private fun emailCompose() {
        val intent = EmailComposeActivity.getStartActivityIntent(this)
        startActivity(intent)
    }
}