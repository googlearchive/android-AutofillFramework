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
import android.widget.Spinner;

import com.example.android.autofillframework.R;

public class CreditCardActivity extends AppCompatActivity {

    public static Intent getStartActivityIntent(Context context) {
        Intent intent = new Intent(context, CreditCardActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.credit_card_activity);

        Spinner ccExpirationDaySpinner = findViewById(R.id.expirationDay);
        Spinner ccExpirationMonthSpinner = findViewById(R.id.expirationMonth);
        Spinner ccExpirationYearSpinner = findViewById(R.id.expirationYear);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource
                (this, R.array.day_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        ccExpirationDaySpinner.setAdapter(dayAdapter);

        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource
                (this, R.array.month_array, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ccExpirationMonthSpinner.setAdapter(monthAdapter);

        ArrayAdapter<CharSequence> yearAdapter = ArrayAdapter.createFromResource
                (this, R.array.year_array, android.R.layout.simple_spinner_item);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ccExpirationYearSpinner.setAdapter(yearAdapter);

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
        //TODO
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
