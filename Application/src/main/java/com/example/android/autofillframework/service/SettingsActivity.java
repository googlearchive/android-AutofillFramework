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
package com.example.android.autofillframework.service;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.example.android.autofillframework.R;

import java.util.Locale;

public class SettingsActivity extends Activity {

    private EditText mNumberDatasets;
    private CheckBox mResponseAuth;
    private CheckBox mDatasetAuth;
    private Button mSave;
    private Button mCancel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_activity);
        mNumberDatasets = (EditText) findViewById(R.id.number_datasets);
        mResponseAuth = (CheckBox) findViewById(R.id.response_auth);
        mDatasetAuth = (CheckBox) findViewById(R.id.dataset_auth);
        mSave = (Button) findViewById(R.id.save);
        mCancel = (Button) findViewById(R.id.cancel);

        final MyPreferences p = MyPreferences.getInstance(this);

        mNumberDatasets.setText(String.format(Locale.getDefault(), "%d", p.getNumberDatasets()));
        mDatasetAuth.setChecked(p.isDatasetAuth());
        mResponseAuth.setChecked(p.isResponseAuth());

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyPreferences.getInstance(SettingsActivity.this).bulkEdit(
                        Integer.parseInt(mNumberDatasets.getText().toString()),
                        mResponseAuth.isChecked(),
                        mDatasetAuth.isChecked());
                finish();
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}