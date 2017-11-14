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
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.autofill.AutofillManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.autofill.app.R;
import com.example.android.autofill.app.WelcomeActivity;

import static com.example.android.autofill.app.Util.TAG;

public class StandardAutoCompleteSignInActivity extends AppCompatActivity {
    private AutoCompleteTextView mUsernameAutoCompleteField;
    private TextView mPasswordField;
    private TextView mLoginButton;
    private TextView mClearButton;
    private boolean mAutofillReceived = false;
    private AutofillManager.AutofillCallback mAutofillCallback;
    private AutofillManager mAutofillManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_with_autocomplete_activity);

        mLoginButton = findViewById(R.id.login);
        mClearButton = findViewById(R.id.clear);
        mUsernameAutoCompleteField = findViewById(R.id.usernameField);
        mPasswordField = findViewById(R.id.passwordField);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFields();
            }
        });
        mAutofillCallback = new MyAutofillCallback();
        mAutofillManager = getSystemService(AutofillManager.class);
        ArrayAdapter<CharSequence> mockAutocompleteAdapter = ArrayAdapter.createFromResource
                (this, R.array.mock_autocomplete_sign_in_suggestions,
                        android.R.layout.simple_dropdown_item_1line);
        mUsernameAutoCompleteField.setAdapter(mockAutocompleteAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAutofillManager.registerCallback(mAutofillCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAutofillManager.unregisterCallback(mAutofillCallback);
    }

    private void resetFields() {
        mUsernameAutoCompleteField.setText("");
        mPasswordField.setText("");
    }

    /**
     * Emulates a login action.
     */
    private void login() {
        String username = mUsernameAutoCompleteField.getText().toString();
        String password = mPasswordField.getText().toString();
        boolean valid = isValidCredentials(username, password);
        if (valid) {
            Intent intent = WelcomeActivity.getStartActivityIntent(StandardAutoCompleteSignInActivity.this);
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

    private class MyAutofillCallback extends AutofillManager.AutofillCallback {
        @Override
        public void onAutofillEvent(@NonNull View view, int event) {
            if (view instanceof AutoCompleteTextView) {
                switch (event) {
                    case AutofillManager.AutofillCallback.EVENT_INPUT_UNAVAILABLE:
                        // no break on purpose
                    case AutofillManager.AutofillCallback.EVENT_INPUT_HIDDEN:
                        if (!mAutofillReceived) {
                            ((AutoCompleteTextView) view).showDropDown();
                        }
                        break;
                    case AutofillManager.AutofillCallback.EVENT_INPUT_SHOWN:
                        mAutofillReceived = true;
                        ((AutoCompleteTextView) view).setAdapter(null);
                        break;
                    default:
                        Log.d(TAG, "Unexpected callback: " + event);
                }
            }
        }
    }
}