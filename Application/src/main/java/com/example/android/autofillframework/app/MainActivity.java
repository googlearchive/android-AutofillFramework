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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.android.autofillframework.R;

/**
 * This is used to launch sample activities that showcase autofill.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NavigationItem loginEditTexts = findViewById(R.id.standardViewSignInButton);
        NavigationItem loginCustomVirtual = findViewById(R.id.virtualViewSignInButton);
        NavigationItem creditCardSpinners = findViewById(R.id.creditCardCheckoutButton);
        NavigationItem loginAutoComplete = findViewById(R.id.standardLoginWithAutoCompleteButton);
        NavigationItem emailCompose = findViewById(R.id.emailComposeButton);

        loginEditTexts.setNavigationButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(StandardSignInActivity.getStartActivityIntent(MainActivity.this));
            }
        });
        loginCustomVirtual.setNavigationButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(VirtualSignInActivity.getStartActivityIntent(MainActivity.this));
            }
        });
        creditCardSpinners.setNavigationButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(CreditCardActivity.getStartActivityIntent(MainActivity.this));
            }
        });
        loginAutoComplete.setNavigationButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(StandardAutoCompleteSignInActivity.getStartActivityIntent(MainActivity.this));
            }
        });
        emailCompose.setNavigationButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(EmailComposeActivity.getStartActivityIntent(MainActivity.this));
            }
        });
    }
}