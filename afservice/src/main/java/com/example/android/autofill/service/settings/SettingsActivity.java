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
package com.example.android.autofill.service.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.autofill.AutofillManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.example.android.autofill.service.AutofillHints;
import com.example.android.autofill.service.R;
import com.example.android.autofill.service.Util;
import com.example.android.autofill.service.datasource.SharedPrefsAutofillRepository;
import com.example.android.autofill.service.datasource.SharedPrefsPackageVerificationRepository;
import com.example.android.autofill.service.model.FilledAutofillFieldCollection;

import static com.example.android.autofill.service.Util.logd;
import static com.example.android.autofill.service.Util.logw;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";
    private static final int REQUEST_CODE_SET_DEFAULT = 1;
    private AutofillManager mAutofillManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multidataset_service_settings_activity);
        mAutofillManager = getSystemService(AutofillManager.class);
        final MyPreferences preferences = MyPreferences.getInstance(this);
        setupSettingsSwitch(R.id.settings_auth_responses_container,
                R.id.settings_auth_responses_label,
                R.id.settings_auth_responses_switch,
                preferences.isResponseAuth(),
                (compoundButton, isResponseAuth) -> preferences.setResponseAuth(isResponseAuth));
        setupSettingsSwitch(R.id.settings_auth_datasets_container,
                R.id.settings_auth_datasets_label,
                R.id.settings_auth_datasets_switch,
                preferences.isDatasetAuth(),
                (compoundButton, isDatasetAuth) -> preferences.setDatasetAuth(isDatasetAuth));
        setupSettingsButton(R.id.settings_add_data_container,
                R.id.settings_add_data_label,
                R.id.settings_add_data_icon,
                (view) -> buildAddDataDialog().show());
        setupSettingsButton(R.id.settings_clear_data_container,
                R.id.settings_clear_data_label,
                R.id.settings_clear_data_icon,
                (view) -> buildClearDataDialog().show());
        setupSettingsButton(R.id.settings_auth_credentials_container,
                R.id.settings_auth_credentials_label,
                R.id.settings_auth_credentials_icon,
                (view) -> {
                    if (preferences.getMasterPassword() != null) {
                        buildCurrentCredentialsDialog().show();
                    } else {
                        buildNewCredentialsDialog().show();
                    }
                });
        setupSettingsSwitch(R.id.settingsSetServiceContainer,
                R.id.settingsSetServiceLabel,
                R.id.settingsSetServiceSwitch,
                mAutofillManager.hasEnabledAutofillServices(),
                (compoundButton, serviceSet) -> setService(serviceSet));
        RadioGroup loggingLevelContainer = findViewById(R.id.loggingLevelContainer);
        Util.LogLevel loggingLevel = preferences.getLoggingLevel();
        Util.setLoggingLevel(loggingLevel);
        switch (loggingLevel) {
            case OFF:
                loggingLevelContainer.check(R.id.loggingOff);
                break;
            case DEBUG:
                loggingLevelContainer.check(R.id.loggingDebug);
                break;
            case VERBOSE:
                loggingLevelContainer.check(R.id.loggingVerbose);
                break;
        }
        loggingLevelContainer.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.loggingOff:
                    preferences.setLoggingLevel(Util.LogLevel.OFF);
                    break;
                case R.id.loggingDebug:
                    preferences.setLoggingLevel(Util.LogLevel.DEBUG);
                    break;
                case R.id.loggingVerbose:
                    preferences.setLoggingLevel(Util.LogLevel.VERBOSE);
                    break;
            }
        });
    }

    private AlertDialog buildClearDataDialog() {
        return new AlertDialog.Builder(SettingsActivity.this)
                .setMessage(R.string.settings_clear_data_confirmation)
                .setTitle(R.string.settings_clear_data_confirmation_title)
                .setNegativeButton(R.string.settings_cancel, null)
                .setPositiveButton(R.string.settings_ok, (dialog, which) -> {
                    SharedPrefsAutofillRepository.getInstance().clear(SettingsActivity.this);
                    SharedPrefsPackageVerificationRepository.getInstance()
                            .clear(SettingsActivity.this);
                    MyPreferences.getInstance(SettingsActivity.this).clearCredentials();
                    dialog.dismiss();
                })
                .create();
    }

    private AlertDialog buildAddDataDialog() {
        NumberPicker numberOfDatasetsPicker = LayoutInflater
                .from(SettingsActivity.this)
                .inflate(R.layout.multidataset_service_settings_add_data_dialog, null)
                .findViewById(R.id.number_of_datasets_picker);
        numberOfDatasetsPicker.setMinValue(0);
        numberOfDatasetsPicker.setMaxValue(10);
        numberOfDatasetsPicker.setWrapSelectorWheel(false);
        return new AlertDialog.Builder(SettingsActivity.this)
                .setTitle(R.string.settings_add_data_title)
                .setNegativeButton(R.string.settings_cancel, null)
                .setMessage(R.string.settings_select_number_of_datasets)
                .setView(numberOfDatasetsPicker)
                .setPositiveButton(R.string.settings_ok, (dialog, which) -> {
                    int numOfDatasets = numberOfDatasetsPicker.getValue();
                    boolean success = buildAndSaveMockedAutofillFieldCollection(
                            SettingsActivity.this, numOfDatasets);
                    dialog.dismiss();
                    if (success) {
                        Snackbar.make(SettingsActivity.this.findViewById(R.id.settings_layout),
                                SettingsActivity.this.getResources().getQuantityString(
                                        R.plurals.settings_add_data_success, numOfDatasets,
                                        numOfDatasets),
                                Snackbar.LENGTH_SHORT).show();
                    }
                })
                .create();
    }

    /**
     * Builds mock autofill data and saves it to repository.
     */
    private boolean buildAndSaveMockedAutofillFieldCollection(Context context, int numOfDatasets) {
        if (numOfDatasets < 0 || numOfDatasets > 10) {
            logw("Number of Datasets (%d) out of range.", numOfDatasets);
            return false;
        }
        for (int i = 0; i < numOfDatasets * 2; i += 2) {
            for (int partition : AutofillHints.PARTITIONS) {
                FilledAutofillFieldCollection filledAutofillFieldCollection =
                        AutofillHints.getFakeFieldCollection(partition, i);
                SharedPrefsAutofillRepository.getInstance().saveFilledAutofillFieldCollection(
                        context, filledAutofillFieldCollection);
            }
        }
        return true;
    }

    private AlertDialog.Builder prepareCredentialsDialog() {
        return new AlertDialog.Builder(SettingsActivity.this)
                .setTitle(R.string.settings_auth_change_credentials_title)
                .setNegativeButton(R.string.settings_cancel, null);
    }

    private AlertDialog buildCurrentCredentialsDialog() {
        final EditText currentPasswordField = LayoutInflater
                .from(SettingsActivity.this)
                .inflate(R.layout.multidataset_service_settings_authentication_dialog, null)
                .findViewById(R.id.master_password_field);
        return prepareCredentialsDialog()
                .setMessage(R.string.settings_auth_enter_current_password)
                .setView(currentPasswordField)
                .setPositiveButton(R.string.settings_ok, new DialogInterface.OnClickListener() {
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
                .inflate(R.layout.multidataset_service_settings_authentication_dialog, null)
                .findViewById(R.id.master_password_field);
        return prepareCredentialsDialog()
                .setMessage(R.string.settings_auth_enter_new_password)
                .setView(newPasswordField)
                .setPositiveButton(R.string.settings_ok, (dialog, which) -> {
                    String password = newPasswordField.getText().toString();
                    MyPreferences.getInstance(SettingsActivity.this).setMasterPassword(password);
                    dialog.dismiss();
                })
                .create();
    }

    private void setupSettingsSwitch(int containerId, int labelId, int switchId, boolean checked,
            CompoundButton.OnCheckedChangeListener checkedChangeListener) {
        ViewGroup container = findViewById(containerId);
        String switchLabel = ((TextView) container.findViewById(labelId)).getText().toString();
        final Switch switchView = container.findViewById(switchId);
        switchView.setContentDescription(switchLabel);
        switchView.setChecked(checked);
        container.setOnClickListener((view) -> switchView.performClick());
        switchView.setOnCheckedChangeListener(checkedChangeListener);
    }

    private void setupSettingsButton(int containerId, int labelId, int imageViewId,
            final View.OnClickListener onClickListener) {
        ViewGroup container = findViewById(containerId);
        TextView buttonLabel = container.findViewById(labelId);
        String buttonLabelText = buttonLabel.getText().toString();
        ImageView imageView = container.findViewById(imageViewId);
        imageView.setContentDescription(buttonLabelText);
        container.setOnClickListener(onClickListener);
    }

    private void setService(boolean enableService) {
        if (enableService) {
            startEnableService();
        } else {
            disableService();
        }
    }

    private void disableService() {
        if (mAutofillManager != null && mAutofillManager.hasEnabledAutofillServices()) {
            mAutofillManager.disableAutofillServices();
            Snackbar.make(findViewById(R.id.settings_layout),
                    R.string.settings_autofill_disabled_message, Snackbar.LENGTH_SHORT).show();
        } else {
            logd("Sample service already disabled.");
        }
    }

    private void startEnableService() {
        if (mAutofillManager != null && !mAutofillManager.hasEnabledAutofillServices()) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE);
            intent.setData(Uri.parse("package:com.example.android.autofill.service"));
            logd(TAG, "enableService(): intent=%s", intent);
            startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT);
        } else {
            logd("Sample service already enabled.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        logd(TAG, "onActivityResult(): req=%s", requestCode);
        switch (requestCode) {
            case REQUEST_CODE_SET_DEFAULT:
                defaultServiceSet(resultCode);
                break;
        }
    }

    private void defaultServiceSet(int resultCode) {
        logd(TAG, "resultCode=%d", resultCode);
        switch (resultCode) {
            case RESULT_OK:
                logd("Autofill service set.");
                Snackbar.make(findViewById(R.id.settings_layout),
                        R.string.settings_autofill_service_set, Snackbar.LENGTH_SHORT)
                        .show();
                break;
            case RESULT_CANCELED:
                logd("Autofill service not selected.");
                Snackbar.make(findViewById(R.id.settings_layout),
                        R.string.settings_autofill_service_not_set, Snackbar.LENGTH_SHORT)
                        .show();
                break;
        }
    }
}
