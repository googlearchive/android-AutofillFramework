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
package com.example.android.autofill.app.edgecases;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.autofill.AutofillManager;
import android.widget.Toast;

import com.example.android.autofill.app.R;
import com.example.android.autofill.app.Util;
import com.example.android.autofill.app.view.autofillable.CustomVirtualView;
import com.example.android.autofill.app.view.autofillable.ScrollableCustomVirtualView;

/**
 * Activity used to demonstrated safe partitioning of data.
 * <p>
 * <p>It has multiple partitions, but only accepts autofill on each partition at time.
 */
/*
 * TODO list
 *
 * - Fix top margin.
 * - Use a combo box to select if credit card expiration date is expressed as date or text.
 * - Use a dedicated TextView (instead of Toast) for error messages.
 * - Use wrap_context to CustomView container.
 * - Use different background color (or borders) for each partition.
 * - Add more partitions (like address) - should match same partitions from service.
 * - Add more hints (like w3c ones) - should match same hints from service.
 */
public class MultiplePartitionsActivity extends AppCompatActivity {

    private ScrollableCustomVirtualView mCustomVirtualView;
    private AutofillManager mAutofillManager;

    private CustomVirtualView.Partition mCredentialsPartition;
    private CustomVirtualView.Partition mCcPartition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.multiple_partitions_activity);

        mCustomVirtualView = findViewById(R.id.custom_view);


        mCredentialsPartition =
                mCustomVirtualView.addPartition(getString(R.string.partition_credentials));
        mCredentialsPartition.addLine("username", View.AUTOFILL_TYPE_TEXT,
                getString(R.string.username_label),
                "         ", false, View.AUTOFILL_HINT_USERNAME);
        mCredentialsPartition.addLine("password", View.AUTOFILL_TYPE_TEXT,
                getString(R.string.password_label),
                "         ", true, View.AUTOFILL_HINT_PASSWORD);

        int ccExpirationType = View.AUTOFILL_TYPE_DATE;
        // TODO: add a checkbox to switch between text / date instead
        Intent intent = getIntent();
        if (intent != null) {
            int newType = intent.getIntExtra("dateType", -1);
            if (newType != -1) {
                ccExpirationType = newType;
                String typeMessage = getString(R.string.message_credit_card_expiration_type,
                        Util.getAutofillTypeAsString(ccExpirationType));
                // TODO: display type in a header or proper status widget
                Toast.makeText(getApplicationContext(), typeMessage, Toast.LENGTH_LONG).show();
            }
        }

        mCcPartition = mCustomVirtualView.addPartition(getString(R.string.partition_credit_card));
        mCcPartition.addLine("ccNumber", View.AUTOFILL_TYPE_TEXT,
                getString(R.string.credit_card_number_label),
                "         ", true, View.AUTOFILL_HINT_CREDIT_CARD_NUMBER);
        mCcPartition.addLine("ccDay", View.AUTOFILL_TYPE_TEXT,
                getString(R.string.credit_card_expiration_day_label),
                "         ", true, View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DAY);
        mCcPartition.addLine("ccMonth", ccExpirationType,
                getString(R.string.credit_card_expiration_month_label),
                "         ", true, View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH);
        mCcPartition.addLine("ccYear", View.AUTOFILL_TYPE_TEXT,
                getString(R.string.credit_card_expiration_year_label),
                "         ", true, View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR);
        mCcPartition.addLine("ccDate", ccExpirationType,
                getString(R.string.credit_card_expiration_date_label),
                "         ", true, View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DATE);
        mCcPartition.addLine("ccSecurityCode", View.AUTOFILL_TYPE_TEXT,
                getString(R.string.credit_card_security_code_label),
                "         ", true, View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE);

        findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetFields();
                mCustomVirtualView.resetPositions();
                mAutofillManager.cancel();
            }
        });
        mAutofillManager = getSystemService(AutofillManager.class);
    }

    private void resetFields() {
        mCredentialsPartition.reset();
        mCcPartition.reset();
        mCustomVirtualView.postInvalidate();
    }
}
