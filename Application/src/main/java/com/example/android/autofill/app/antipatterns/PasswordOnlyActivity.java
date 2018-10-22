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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.example.android.autofill.app.R;
import com.example.android.autofill.app.WelcomeActivity;

/**
 * This activity is the second step in a multi-screen login workflow that uses 2 distinct activities
 * for username and password, which causes 2 Autofill Save UI to be shown.
 *
 * <p>This is a bad pattern anyways&mdash;apps should use Fragments in such scenarios.
 */
/*
 * TODO list
 * - use ConstraintLayout
 * - use strings.xml insteaf of hardcoded strings
 * - add icon with information
 * - extend AppCompatActivity
 */
public final class PasswordOnlyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_only_activity);

        findViewById(R.id.login).setOnClickListener((v) -> login());
    }

    protected int getContentView() {
        return R.layout.password_only_activity;
    }

    void login() {
        startActivity(new Intent(this, WelcomeActivity.class));
        finish();
    }
}
