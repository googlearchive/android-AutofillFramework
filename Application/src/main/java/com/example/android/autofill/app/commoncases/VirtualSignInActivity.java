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
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.autofill.AutofillManager;
import android.widget.Toast;

import com.example.android.autofill.app.R;
import com.example.android.autofill.app.WelcomeActivity;
import com.example.android.autofill.app.view.autofillable.CustomVirtualView;

/**
 * Activity that uses a virtual views for Username/Password text fields.
 */
public class VirtualSignInActivity extends AppCompatActivity {

    private CustomVirtualView mCustomVirtualView;
    private AutofillManager mAutofillManager;
    private CustomVirtualView.Line mUsernameLine;
    private CustomVirtualView.Line mPasswordLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.virtual_login_activity);

        mCustomVirtualView = (CustomVirtualView) findViewById(R.id.custom_view);

        CustomVirtualView.Partition credentialsPartition =
                mCustomVirtualView.addPartition(getString(R.string.partition_credentials));
        mUsernameLine = credentialsPartition.addLine("username", View.AUTOFILL_TYPE_TEXT,
                getString(R.string.username_label),
                "         ", false, View.AUTOFILL_HINT_USERNAME);
        mPasswordLine = credentialsPartition.addLine("password", View.AUTOFILL_TYPE_TEXT,
                getString(R.string.password_label),
                "         ", true, View.AUTOFILL_HINT_PASSWORD);

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
        findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetFields();
                mAutofillManager.cancel();
            }
        });
        mAutofillManager = getSystemService(AutofillManager.class);
    }

    private void resetFields() {
        mUsernameLine.reset();
        mPasswordLine.reset();
        mCustomVirtualView.postInvalidate();
    }

    /**
     * Emulates a login action.
     */
    private void login() {
        String username = mUsernameLine.getText().toString();
        String password = mPasswordLine.getText().toString();
        boolean valid = isValidCredentials(username, password);
        if (valid) {
            Intent intent = WelcomeActivity.getStartActivityIntent(VirtualSignInActivity.this);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Dummy implementation for demo purposes. A real service should use secure mechanisms to
     * authenticate users.
     */
    public boolean isValidCredentials(String username, String password) {
        return username != null && password != null && username.equals(password);
    }
}
