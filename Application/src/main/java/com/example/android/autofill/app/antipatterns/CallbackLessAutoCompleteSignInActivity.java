/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.example.android.autofill.app.antipatterns;

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
import com.example.android.autofill.app.view.widget.InfoButton;

import static com.example.android.autofill.app.Util.TAG;

public class CallbackLessAutoCompleteSignInActivity extends AppCompatActivity {
    private AutoCompleteTextView mUsernameAutoCompleteField;
    private TextView mPasswordField;
    private TextView mLoginButton;
    private TextView mClearButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_with_autocomplete_activity);

        TextView title = findViewById(R.id.standard_login_header);
        title.setText(R.string.navigation_button_anti_pattern_callbackless_autocomplete_login_label);

        InfoButton info = findViewById(R.id.imageButton);
        info.setInfoText(getString(R.string.anti_pattern_callbackless_autocomplete_login_info));


        mLoginButton = findViewById(R.id.login);
        mClearButton = findViewById(R.id.clear);
        mUsernameAutoCompleteField = findViewById(R.id.usernameField);
        mPasswordField = findViewById(R.id.passwordField);
        mLoginButton.setOnClickListener((v) -> login());
        mClearButton.setOnClickListener((v) -> {
            AutofillManager afm = getSystemService(AutofillManager.class);
            if (afm != null) {
                afm.cancel();
            }
            resetFields();
        });
        ArrayAdapter<CharSequence> mockAutocompleteAdapter = ArrayAdapter.createFromResource
                (this, R.array.mock_autocomplete_sign_in_suggestions,
                        android.R.layout.simple_dropdown_item_1line);
        mUsernameAutoCompleteField.setAdapter(mockAutocompleteAdapter);
        mUsernameAutoCompleteField.setThreshold(1);

        // Show it right away
        mUsernameAutoCompleteField.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mUsernameAutoCompleteField.showDropDown();
            }
        });
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
            Intent intent = WelcomeActivity.getStartActivityIntent(CallbackLessAutoCompleteSignInActivity.this);
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