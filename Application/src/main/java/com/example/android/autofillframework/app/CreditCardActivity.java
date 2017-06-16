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
package com.example.android.autofillframework.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.android.autofillframework.R;

import java.util.Calendar;

public class CreditCardActivity extends AppCompatActivity {

    private static final int CC_EXP_YEARS_COUNT = 5;

    private final String[] years = new String[CC_EXP_YEARS_COUNT];

    private Spinner mCcExpirationDaySpinner;
    private Spinner mCcExpirationMonthSpinner;
    private Spinner mCcExpirationYearSpinner;
    private EditText mCcCardNumber;

    public static Intent getStartActivityIntent(Context context) {
        Intent intent = new Intent(context, CreditCardActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.credit_card_activity);
        mCcExpirationDaySpinner = findViewById(R.id.expirationDay);
        mCcExpirationMonthSpinner = findViewById(R.id.expirationMonth);
        mCcExpirationYearSpinner = findViewById(R.id.expirationYear);
        mCcCardNumber = findViewById(R.id.creditCardNumberField);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource
                (this, R.array.day_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mCcExpirationDaySpinner.setAdapter(dayAdapter);

        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource
                (this, R.array.month_array, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCcExpirationMonthSpinner.setAdapter(monthAdapter);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 0; i < years.length; i++) {
            years[i] = Integer.toString(year + i);
        }
        mCcExpirationYearSpinner.setAdapter(
                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, years) {
                    @Override
                    public CharSequence[] getAutofillOptions() {
                        return years;
                    }
                });
        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
        findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFields();
            }
        });
    }

    private void resetFields() {
        mCcExpirationDaySpinner.setSelection(0);
        mCcExpirationMonthSpinner.setSelection(0);
        mCcExpirationYearSpinner.setSelection(0);
        mCcCardNumber.setText("");
    }

    /**
     * Launches new Activity and finishes, triggering an autofill save request if the user entered
     * any new data.
     */
    private void submit() {
        Intent intent = WelcomeActivity.getStartActivityIntent(CreditCardActivity.this);
        startActivity(intent);
        finish();
    }
}
