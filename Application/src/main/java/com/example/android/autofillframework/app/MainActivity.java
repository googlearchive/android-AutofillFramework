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

import android.app.Activity;
import android.content.Intent;
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
        findViewById(R.id.standardViewSignInButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                standardViewSignIn();
            }
        });
        findViewById(R.id.virtualViewSignInButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                virtualViewSignIn();
            }
        });
        findViewById(R.id.creditCardCheckoutButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                creditCardCheckout();
            }
        });
    }

    private void creditCardCheckout() {
        Intent intent = CreditCardActivity.getStartActivityIntent(this);
        startActivity(intent);
    }

    private void standardViewSignIn() {
        Intent intent = LoginActivity.getStartActivityIntent(this);
        startActivity(intent);
    }

    private void virtualViewSignIn() {
        Intent intent = VirtualLoginActivity.getStartActivityIntent(this);
        startActivity(intent);
    }
}