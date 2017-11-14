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
package com.example.android.autofill.app.commoncases;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.autofill.AutofillManager;
import android.widget.EditText;

import com.example.android.autofill.app.R;
import com.example.android.autofill.app.WelcomeActivity;
import com.example.android.autofill.app.view.autofillable.CreditCardExpirationDatePickerView;

import static com.example.android.autofill.app.Util.TAG;

public class CreditCardDatePickerActivity extends AppCompatActivity {

    private CreditCardExpirationDatePickerView mCcExpDateView;
    private EditText mCcExpNumber;
    private EditText mCcSecurityCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.credit_card_date_picker_activity);
        mCcExpDateView = findViewById(R.id.creditCardExpirationView);
        mCcExpNumber = findViewById(R.id.creditCardNumberField);
        mCcSecurityCode = findViewById(R.id.creditCardSecurityCode);
        findViewById(R.id.submitButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
        findViewById(R.id.clearButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSystemService(AutofillManager.class).cancel();
                resetFields();
            }
        });

        mCcExpDateView.reset();
    }

    private void resetFields() {
        mCcExpDateView.reset();
        mCcExpNumber.setText("");
        mCcSecurityCode.setText("");
    }

    public void showDatePickerDialog(View v) {
        if (v != mCcExpDateView) {
            Log.w(TAG, "showDatePickerDialog() called on invalid view: " + v);
            return;
        }
        mCcExpDateView.showDatePickerDialog(getSupportFragmentManager());
    }


    /**
     * Launches new Activity and finishes, triggering an autofill save request if the user entered
     * any new data.
     */
    private void submit() {
        Intent intent = WelcomeActivity.getStartActivityIntent(this);
        startActivity(intent);
        finish();
    }
}
