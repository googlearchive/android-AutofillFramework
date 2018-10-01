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
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.autofill.AutofillManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.autofill.app.R;
import com.example.android.autofill.app.WelcomeActivity;

/**
 * This activity behaves the same as
 * {@link com.example.android.autofill.app.commoncases.StandardSignInActivity}, except that it
 * creates its view structure on {@link #onStart()}.
 *
 * <p>This is a bad pattern anyways&mdash;the view structure should be created on
 * {@code onCreate()}&mdash;but it's aggravatted on autofill because when an autofill service
 * requires authentication, the Android System launches a new activity to handle authentication
 * using this activity's task. When the authentication acivity finishes, this activity is
 * resumed, hence if {@link #onStart()} (or {@code onResume()}) re-generates the view structure,
 * it invalidates the response sent by the autofill service, which triggers a new autofill request
 * when a field is focused again.
 */
public class BadViewStructureCreationSignInActivity extends AppCompatActivity {

    private EditText mUsernameEditText;
    private EditText mPasswordEditText;

    @Override
    protected void onStart() {
        super.onStart();

        setContentView(R.layout.login_activity);
        mUsernameEditText = findViewById(R.id.usernameField);
        mPasswordEditText = findViewById(R.id.passwordField);
        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AutofillManager afm = getSystemService(AutofillManager.class);
                if (afm != null) {
                    afm.cancel();
                }
                resetFields();
            }
        });
    }

    private void resetFields() {
        mUsernameEditText.setText("");
        mPasswordEditText.setText("");
    }

    /**
     * Emulates a login action.
     */
    private void login() {
        String username = mUsernameEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        boolean valid = isValidCredentials(username, password);
        if (valid) {
            Intent intent = WelcomeActivity
                    .getStartActivityIntent(BadViewStructureCreationSignInActivity.this);
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
        return username != null && password != null && username.equalsIgnoreCase(password);
    }
}
