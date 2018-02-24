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
package com.example.android.autofill.app.view.autofillable;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.autofill.AutofillManager;
import android.view.autofill.AutofillValue;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;

import com.example.android.autofill.app.R;

import java.util.Calendar;

import static com.example.android.autofill.app.Util.TAG;

/**
 * A custom view that represents a {@link View#AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DATE} using
 * 2 {@link Spinner spinners} to represent the credit card expiration month and year.
 */
public class CreditCardExpirationDateCompoundView extends FrameLayout {

    private static final int CC_EXP_YEARS_COUNT = 5;

    private final String[] mYears = new String[CC_EXP_YEARS_COUNT];

    private Spinner mCcExpMonthSpinner;
    private Spinner mCcExpYearSpinner;

    public CreditCardExpirationDateCompoundView(@NonNull Context context) {
        this(context, null);
    }

    public CreditCardExpirationDateCompoundView(@NonNull Context context,
            @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CreditCardExpirationDateCompoundView(@NonNull Context context,
            @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CreditCardExpirationDateCompoundView(@NonNull final Context context,
            @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        View rootView = LayoutInflater.from(context).inflate(R.layout.cc_exp_date, this);
        mCcExpMonthSpinner = rootView.findViewById(R.id.ccExpMonth);
        mCcExpYearSpinner = rootView.findViewById(R.id.ccExpYear);
        setImportantForAutofill(IMPORTANT_FOR_AUTOFILL_YES_EXCLUDE_DESCENDANTS);
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource
                (context, R.array.month_array, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCcExpMonthSpinner.setAdapter(monthAdapter);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 0; i < mYears.length; i++) {
            mYears[i] = Integer.toString(year + i);
        }
        mCcExpYearSpinner.setAdapter(new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, mYears));
        AdapterView.OnItemSelectedListener onItemSelectedListener =
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        context.getSystemService(AutofillManager.class)
                                .notifyValueChanged(CreditCardExpirationDateCompoundView.this);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                };
        mCcExpMonthSpinner.setOnItemSelectedListener(onItemSelectedListener);
        mCcExpYearSpinner.setOnItemSelectedListener(onItemSelectedListener);
    }

    @Override
    public AutofillValue getAutofillValue() {
        Calendar calendar = Calendar.getInstance();
        // Set hours, minutes, seconds, and millis to 0 to ensure getAutofillValue() == the value
        // set by autofill(). Without this line, the view will not turn yellow when updated.
        calendar.clear();
        int year = Integer.parseInt(mCcExpYearSpinner.getSelectedItem().toString());
        int month = mCcExpMonthSpinner.getSelectedItemPosition();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        long unixTime = calendar.getTimeInMillis();
        return AutofillValue.forDate(unixTime);
    }

    @Override
    public void autofill(AutofillValue value) {
        if (!value.isDate()) {
            Log.w(TAG, "Ignoring autofill() because service sent a non-date value:" + value);
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(value.getDateValue());
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        mCcExpMonthSpinner.setSelection(month);
        mCcExpYearSpinner.setSelection(year - Integer.parseInt(mYears[0]));
    }

    @Override
    public int getAutofillType() {
        return AUTOFILL_TYPE_DATE;
    }

    public void reset() {
        mCcExpMonthSpinner.setSelection(0);
        mCcExpYearSpinner.setSelection(0);
    }
}