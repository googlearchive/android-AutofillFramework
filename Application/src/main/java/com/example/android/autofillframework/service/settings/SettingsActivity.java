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
package com.example.android.autofillframework.service.settings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.android.autofillframework.R;
import com.example.android.autofillframework.service.datasource.SharedPrefsAutofillRepository;

public class SettingsActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        final MyPreferences preferences = MyPreferences.getInstance(this);
        setupSettingsSwitch(R.id.settings_auth_responses_container,
                R.id.settings_auth_responses_label,
                R.id.settings_auth_responses_switch,
                preferences.isResponseAuth(),
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        preferences.setResponseAuth(b);
                    }
                });
        setupSettingsSwitch(R.id.settings_auth_datasets_container,
                R.id.settings_auth_datasets_label,
                R.id.settings_auth_datasets_switch,
                preferences.isDatasetAuth(),
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        preferences.setDatasetAuth(b);
                    }
                });
        setupSettingsButton(R.id.settings_clear_data_container,
                R.id.settings_clear_data_label,
                R.id.settings_clear_data_icon,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        buildClearDataDialog().show();
                    }
                });
        setupSettingsButton(R.id.settings_auth_credentials_container,
                R.id.settings_auth_credentials_label,
                R.id.settings_auth_credentials_icon,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (preferences.getMasterPassword() != null) {
                            buildCurrentCredentialsDialog().show();
                        } else {
                            buildNewCredentialsDialog().show();
                        }
                    }
                });
    }

    private AlertDialog buildClearDataDialog() {
        return new AlertDialog.Builder(SettingsActivity.this)
                .setMessage(R.string.settings_clear_data_confirmation)
                .setTitle(R.string.settings_clear_data_confirmation_title)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPrefsAutofillRepository.getInstance
                                (SettingsActivity.this).clear();
                        MyPreferences.getInstance(SettingsActivity.this)
                                .clearCredentials();
                        dialog.dismiss();
                    }
                })
                .create();
    }

    private AlertDialog.Builder prepareCredentialsDialog() {
        return new AlertDialog.Builder(SettingsActivity.this)
                .setTitle(R.string.settings_auth_change_credentials_title)
                .setNegativeButton(R.string.cancel, null);
    }

    private AlertDialog buildCurrentCredentialsDialog() {
        final EditText currentPasswordField = LayoutInflater
                .from(SettingsActivity.this)
                .inflate(R.layout.settings_authentication_dialog, null)
                .findViewById(R.id.master_password_field);
        return prepareCredentialsDialog()
                .setMessage(R.string.settings_auth_enter_current_password)
                .setView(currentPasswordField)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String password = currentPasswordField.getText().toString();
                        if (MyPreferences.getInstance(SettingsActivity.this).getMasterPassword()
                                .equals(password)) {
                            buildNewCredentialsDialog().show();
                            dialog.dismiss();
                        }
                    }
                })
                .create();
    }

    private AlertDialog buildNewCredentialsDialog() {
        final EditText newPasswordField = LayoutInflater
                .from(SettingsActivity.this)
                .inflate(R.layout.settings_authentication_dialog, null)
                .findViewById(R.id.master_password_field);
        return prepareCredentialsDialog()
                .setMessage(R.string.settings_auth_enter_new_password)
                .setView(newPasswordField)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String password = newPasswordField.getText().toString();
                        MyPreferences.getInstance(SettingsActivity.this).setMasterPassword(password);
                        dialog.dismiss();
                    }
                })
                .create();
    }

    private void setupSettingsSwitch(int containerId, int labelId, int switchId, boolean checked,
            CompoundButton.OnCheckedChangeListener checkedChangeListener) {
        ViewGroup container = (ViewGroup) findViewById(containerId);
        String switchLabel = ((TextView) container.findViewById(labelId)).getText().toString();
        final Switch switchView = container.findViewById(switchId);
        switchView.setContentDescription(switchLabel);
        switchView.setChecked(checked);
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchView.performClick();
            }
        });
        switchView.setOnCheckedChangeListener(checkedChangeListener);
    }

    private void setupSettingsButton(int containerId, int labelId, int imageViewId,
            final View.OnClickListener onClickListener) {
        ViewGroup container = (ViewGroup) findViewById(containerId);
        String buttonLabel = ((TextView) container.findViewById(labelId)).getText().toString();
        final ImageView imageView = container.findViewById(imageViewId);
        imageView.setContentDescription(buttonLabel);
        container.setOnClickListener(onClickListener);
    }
}
